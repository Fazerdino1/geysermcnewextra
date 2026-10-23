package fr.crossplaymc.geysermcnewextra.core.config;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

/**
 * The project was renamed, so its data folder changed name too. On the first start under the new
 * name, copy the previous folder (config, player preferences, optional packs, skins) so nothing is
 * lost. The old folder is left untouched as a backup.
 */
public final class DataFolderMigration {
    private DataFolderMigration() {
    }

    /**
     * @return true when the legacy folder was copied
     */
    public static boolean copyLegacyFolder(Path legacy, Path current) throws IOException {
        if (!Files.isDirectory(legacy) || legacy.toAbsolutePath().normalize().equals(current.toAbsolutePath().normalize())) {
            return false;
        }
        if (Files.isDirectory(current) && !isEmpty(current)) {
            return false;
        }
        Files.createDirectories(current);
        Files.walkFileTree(legacy, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(current.resolve(legacy.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, current.resolve(legacy.relativize(file).toString()), StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
        return true;
    }

    private static boolean isEmpty(Path directory) throws IOException {
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.findAny().isEmpty();
        }
    }
}
