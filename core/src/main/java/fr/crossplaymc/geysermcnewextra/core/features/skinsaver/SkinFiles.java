package fr.crossplaymc.geysermcnewextra.core.features.skinsaver;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Checks for skin data sent by Bedrock clients. Skin/cape ids and image sizes come straight
 * from the client, so they must never be trusted as file names or allocation sizes.
 */
public final class SkinFiles {
    // Bedrock skins are at most 512x512 (HD skins); anything larger is not a real skin
    public static final int MAX_IMAGE_SIZE = 512;
    private static final int MAX_NAME_LENGTH = 64;
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9._-]+");

    private SkinFiles() {
    }

    /**
     * @return the id itself when it is a plain, short file name, otherwise a hash of it
     */
    public static String safeFolderName(String id) {
        if (id != null && id.length() <= MAX_NAME_LENGTH && SAFE_NAME.matcher(id).matches() && !id.matches("\\.+")) {
            return id;
        }
        return "id-" + sha256(id == null ? "" : id).substring(0, 32);
    }

    /**
     * @return {@code base/name}, or null if it would end up outside {@code base}
     */
    @Nullable
    public static Path resolveInside(Path base, String name) {
        Path root = base.toAbsolutePath().normalize();
        Path resolved = root.resolve(name).normalize();
        return resolved.startsWith(root) && !resolved.equals(root) ? resolved : null;
    }

    public static boolean isValidImage(int width, int height, int dataLength) {
        if (width <= 0 || height <= 0 || width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
            return false;
        }
        return dataLength >= width * height * 4;
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }
}
