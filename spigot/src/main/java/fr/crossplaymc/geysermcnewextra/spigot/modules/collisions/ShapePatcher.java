package fr.crossplaymc.geysermcnewextra.spigot.modules.collisions;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

/**
 * Replaces block shapes of the server (static final {@code VoxelShape} fields) with an empty shape.
 * Works on servers running with Mojang's names (Paper 1.20.5+ and its forks such as Purpur); on others the
 * classes are not found and the module stays off. The approach comes from Hurricane by GeyserMC (MIT).
 */
final class ShapePatcher {
    interface Action {
        void run() throws ReflectiveOperationException;
    }

    /**
     * A replaced field, with what it held before.
     */
    final class Patch {
        private final Field field;
        private final Object original;
        private final Object block;

        private Patch(Field field, Object original, Object block) {
            this.field = field;
            this.original = original;
            this.block = block;
        }

        void undo() {
            write(field, original);
            refreshCaches(block);
        }
    }

    private static final String BLOCKS = "net.minecraft.world.level.block.";

    private final Unsafe unsafe;
    private final Class<?> voxelShape;
    private final Object emptyShape;

    ShapePatcher() throws ReflectiveOperationException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        unsafe = (Unsafe) theUnsafe.get(null);
        voxelShape = Class.forName("net.minecraft.world.phys.shapes.VoxelShape");
        emptyShape = Class.forName("net.minecraft.world.phys.shapes.Shapes").getMethod("empty").invoke(null);
    }

    List<Patch> removeBambooCollision() throws ReflectiveOperationException {
        Class<?> bamboo = Class.forName(BLOCKS + "BambooStalkBlock");
        List<Field> shapes = staticShapes(bamboo);
        // Named SHAPE_COLLISION in recent versions; otherwise, the only shape as thin as bamboo's collision box
        Field collision = shapes.stream()
                .filter(f -> f.getName().equals("SHAPE_COLLISION") || f.getName().equals("COLLISION_SHAPE"))
                .findFirst()
                .orElse(null);
        if (collision == null) {
            for (Field field : shapes) {
                if (isBambooCollisionBox(read(field))) {
                    collision = field;
                    break;
                }
            }
        }
        if (collision == null) {
            throw new NoSuchFieldException(bamboo.getName() + " collision shape (fields: " + names(shapes) + ")");
        }
        return List.of(replace(collision, block("BAMBOO")));
    }

    List<Patch> removeDripstoneCollision() throws ReflectiveOperationException {
        Class<?> dripstone = Class.forName(BLOCKS + "PointedDripstoneBlock");
        // One shape per thickness and direction (tip merge, tip up, tip down, frustum, middle, base); the shape
        // used to let water drip through is not a collision shape
        List<Field> shapes = staticShapes(dripstone).stream()
                .filter(f -> !f.getName().toUpperCase(Locale.ROOT).contains("REQUIRED"))
                .toList();
        if (shapes.size() != 6) {
            throw new NoSuchFieldException(dripstone.getName() + " has " + shapes.size() + " shapes instead of 6 (" + names(shapes) + ")");
        }
        Object block = block("POINTED_DRIPSTONE");
        List<Patch> done = new ArrayList<>();
        for (Field field : shapes) {
            done.add(replace(field, block));
        }
        return done;
    }

    private Patch replace(Field field, Object block) {
        Patch patch = new Patch(field, read(field), block);
        write(field, emptyShape);
        refreshCaches(block);
        return patch;
    }

    private List<Field> staticShapes(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && voxelShape.isAssignableFrom(f.getType()))
                .toList();
    }

    private Object read(Field field) {
        return unsafe.getObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
    }

    private void write(Field field, Object value) {
        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
    }

    private boolean isBambooCollisionBox(Object shape) {
        try {
            Object bounds = voxelShape.getMethod("bounds").invoke(shape);
            double minX = bounds.getClass().getField("minX").getDouble(bounds);
            double maxX = bounds.getClass().getField("maxX").getDouble(bounds);
            return Math.abs(minX - 6.5 / 16) < 1e-6 && Math.abs(maxX - 9.5 / 16) < 1e-6;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static Object block(String name) {
        try {
            return Class.forName(BLOCKS + "Blocks").getField(name).get(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Block states keep a cache of their collision shape, filled at startup: rebuild it.
     */
    private static void refreshCaches(Object block) {
        if (block == null) {
            return;
        }
        try {
            Object definition = block.getClass().getMethod("getStateDefinition").invoke(block);
            for (Object state : (Iterable<?>) definition.getClass().getMethod("getPossibleStates").invoke(definition)) {
                Method initCache = state.getClass().getMethod("initCache");
                initCache.invoke(state);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            // The shapes are read again for players anyway; only the cached copy may be stale
            SERVER.debug("Could not refresh block state caches: " + e);
        }
    }

    private static String names(List<Field> fields) {
        return String.join(", ", fields.stream().map(Field::getName).toList());
    }
}
