package fr.crossplaymc.geysermcnewextra.core.features.skinsaver;

import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import org.geysermc.geyser.session.auth.BedrockClientData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class SkinSaver {
    // Geometry/animation JSON written to disk; real skins stay far below this
    private static final int MAX_TEXT_BYTES = 2 * 1024 * 1024;

    public static void save(ExtrasPlayer player) {
        try {
            // Skins
            BedrockClientData data = player.getSession().getClientData();
            // Skin and cape ids are chosen by the client: never use them as paths as-is
            Path skinsFolder = createFolder(SERVER.getPluginFolder().resolve("skins"),
                    SkinFiles.safeFolderName(data.getSkinId().replace(data.getCapeId(), "")));
            if (skinsFolder == null) {
                SERVER.warn(Lang.console("gmne.console.skin_invalid_id", player.getBedrockXUID()));
                return;
            }

            // BedrockClientData already Base64-decodes these fields (StringToByteDeserializer)
            byte[] skinBytes = data.getSkinData();
            if (SkinFiles.isValidImage(data.getSkinImageWidth(), data.getSkinImageHeight(), skinBytes.length)) {
                File skinFile = skinsFolder.resolve("skin.png").toFile();
                ImageIO.write(getFromSkinData(data.getSkinImageWidth(), data.getSkinImageHeight(), skinBytes), "png", skinFile);
            } else {
                SERVER.warn(Lang.console("gmne.console.skin_bad_image", player.getBedrockXUID(),
                        data.getSkinImageWidth(), data.getSkinImageHeight(), skinBytes.length));
            }

            String rawAnimData = data.getSkinAnimationData();
            if (!rawAnimData.isEmpty() && !rawAnimData.isBlank()) {
                String animationData = new String(Base64.getDecoder().decode(rawAnimData.getBytes(StandardCharsets.UTF_8)));
                if (!animationData.isEmpty() && !animationData.isBlank()) {
                    writeText(player, skinsFolder.resolve("skin.animation.json"), animationData);
                }
            }

            writeText(player, skinsFolder.resolve("skin.geometry.json"), new String(data.getGeometryData(), StandardCharsets.UTF_8));
            writeText(player, skinsFolder.resolve("skin.geometry_name.json"), new String(data.getGeometryName(), StandardCharsets.UTF_8));
            // Capes
            if (!data.getCapeId().isEmpty()) {
                Path capesFolder = createFolder(SERVER.getPluginFolder().resolve("capes"), SkinFiles.safeFolderName(data.getCapeId()));
                byte[] capeBytes = data.getCapeData();
                if (capesFolder != null && SkinFiles.isValidImage(data.getCapeImageWidth(), data.getCapeImageHeight(), capeBytes.length)) {
                    File capeFile = capesFolder.resolve("cape.png").toFile();
                    ImageIO.write(getFromSkinData(data.getCapeImageWidth(), data.getCapeImageHeight(), capeBytes), "png", capeFile);
                } else {
                    SERVER.warn(Lang.console("gmne.console.cape_invalid", player.getBedrockXUID()));
                }
            }

        } catch (IOException | RuntimeException e) {
            SERVER.warn(Lang.console("gmne.console.skin_save_failed", player.getBedrockXUID(), e));
        }
    }

    private static Path createFolder(Path base, String name) throws IOException {
        Path folder = SkinFiles.resolveInside(base, name);
        return folder == null ? null : Files.createDirectories(folder);
    }

    private static void writeText(ExtrasPlayer player, Path file, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_TEXT_BYTES) {
            SERVER.warn(Lang.console("gmne.console.skin_file_too_big", file.getFileName(), player.getBedrockXUID(), bytes.length));
            return;
        }
        Files.write(file, bytes);
    }

    private static BufferedImage getFromSkinData(int width, int height, byte[] pixelData) {
        BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixelData[i * 4 + 3] & 0xFF) << 24; // Alpha
            int r = (pixelData[i * 4] & 0xFF) << 16; // Red
            int g = (pixelData[i * 4 + 1] & 0xFF) << 8;  // Green
            int b = (pixelData[i * 4 + 2] & 0xFF);      // Blue
            pixels[i] = a | r | g | b;
        }

        argbImage.setRGB(0, 0, width, height, pixels, 0, width);
        return argbImage;
    }
}
