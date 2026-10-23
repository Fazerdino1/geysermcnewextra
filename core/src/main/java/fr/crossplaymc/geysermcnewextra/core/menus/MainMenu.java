package fr.crossplaymc.geysermcnewextra.core.menus;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockMenu;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Button;
import fr.crossplaymc.geysermcnewextra.core.locale.BedrockLocale;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.menus.SettingsMenu;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.packs.PackLoader;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.packs.PackMenu;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.serverlinks.ServerLinksMenu;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Action;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.protocol.bedrock.data.camera.AimAssistAction;
import org.cloudburstmc.protocol.bedrock.packet.CameraAimAssistPacket;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class MainMenu extends BedrockMenu {
    public MainMenu() {
        super();
    }

    @Override
    public SimpleForm create(ExtrasPlayer player) {
        setTitle(player.translateGE("ge.menu"));
        add(new Button(Action.SWAP_OFFHAND.translate(player), FormImage.Type.PATH, "textures/ui/move.png", () -> {
            Action.SWAP_OFFHAND.run(player);
        }));

        add(new Button(Action.RECONNECT.translate(player), FormImage.Type.PATH, "textures/ui/refresh_hover.png", () -> {
            Action.RECONNECT.run(player);
        }));

        add(new Button(BedrockLocale.SETTINGS, FormImage.Type.PATH, "textures/ui/settings_glyph_color_2x.png", () -> {
            player.sendForm(new SettingsMenu());
        }));

        add(new Button(Action.OPEN_ADVANCEMENTS.translate(player), FormImage.Type.PATH, "textures/ui/achievements.png", () -> {
            Action.OPEN_ADVANCEMENTS.run(player);
        }));

        add(new Button(Action.OPEN_STATISTICS.translate(player), FormImage.Type.PATH, "textures/ui/world_glyph_color_2x_black_outline.png", () -> {
            Action.OPEN_STATISTICS.run(player);
        }));

        add(new Button(Action.PLAYER_LIST.translate(player), FormImage.Type.PATH, "textures/ui/Local.png", () -> {
            Action.PLAYER_LIST.run(player);
        }));

        if (!player.getServerLinksData().getServerLinks().isEmpty()) {
            add(new Button(player.translate("menu.server_links.title"), FormImage.Type.PATH, "textures/ui/external_link.png", () -> {
                player.sendForm(new ServerLinksMenu());
            }));
        }

        if (!PackLoader.PACKS.isEmpty()) {
            add(new Button(BedrockLocale.MENU.RESOURCE_PACKS, FormImage.Type.PATH, "textures/ui/glyph_resource_pack.png", () -> {
                player.sendForm(new PackMenu());
            }));
        }

        if (GE.getConfig().isDebugMode()) {
            /*
            add(new Button("EmotesTest", FormImage.Type.PATH, "textures/ui/emotes.png", () -> {
                player.sendForm(new EmoteDataTestMenu());
            }));

             */
        }



        return super.create(player);
    }
}
