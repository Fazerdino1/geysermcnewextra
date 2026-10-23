package fr.crossplaymc.geysermcnewextra.core.menus.settings.sections;

import fr.crossplaymc.geysermcnewextra.core.version.PluginVersion;
import fr.crossplaymc.geysermcnewextra.ServerType;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.elements.*;
import fr.crossplaymc.geysermcnewextra.core.locale.BedrockLocale;
import fr.crossplaymc.geysermcnewextra.core.menus.Menus;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.auth.BedrockClientData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DebugSection extends Section {
    @Override
    public void build(BedrockForm menu, GeyserSession session, ExtrasPlayer player) {
        HashMap<String, Menus> menuNames = new HashMap<>();
        for (Menus menuOption : Menus.values()) {
            menuNames.put(menuOption.name(), menuOption);
        }
        menu.add(new Dropdown(player.lang("gmne.debug.settings_menu_form"),
                new ArrayList<>(menuNames.keySet()),
                player.getPreferences().getSettingsMenuForm().name(),
                (str) -> player.getPreferences().setSettingsMenuForm(Menus.valueOf(str))
        ));
        BedrockClientData data = session.getClientData();
        menu.add(new SubSectionLabel(player.lang("gmne.debug.client_data")));
        menu.add(new Label(player.lang("gmne.debug.game_version", data.getGameVersion())));
        menu.add(new Label(player.lang("gmne.debug.username", data.getUsername())));
        menu.add(new Label(player.lang("gmne.debug.offline_id", data.getPlatformOfflineId())));
        menu.add(new Label(player.lang("gmne.debug.online_id", data.getPlatformOnlineId())));
        menu.add(new Label(player.lang("gmne.debug.playfab_id", session.playFabId())));
        menu.add(new Label(player.lang("gmne.debug.device_id", data.getDeviceId())));
        menu.add(new Label(player.lang("gmne.debug.device_model", data.getDeviceModel())));
        menu.add(new Label(player.lang("gmne.debug.gui_scale", data.getGuiScale())));
        menu.add(new Label(player.lang("gmne.debug.joined_address", data.getServerAddress())));
        menu.add(new Label(player.lang("gmne.debug.language_code", data.getLanguageCode())));

        // Cape ID is appended to the Skin ID
        menu.add(new Label(player.lang("gmne.debug.skin_id", data.getSkinId().replace(data.getCapeId(), ""))));
        menu.add(new Label(player.lang("gmne.debug.skin_color", data.getSkinColor())));
        menu.add(new Label(player.lang("gmne.debug.arm_size", data.getArmSize())));
        menu.add(new Label(player.lang("gmne.debug.cape_id", data.getCapeId())));
        menu.add(new Label(player.lang("gmne.debug.cape_on_classic", data.isCapeOnClassicSkin())));
        menu.add(new Label(player.lang("gmne.debug.persona_skin", data.isPersonaSkin())));
        menu.add(new Label(player.lang("gmne.debug.premium_skin", data.isPremiumSkin())));

        menu.add(new SubSectionLabel(player.lang("gmne.debug.player_data")));
        menu.add(new Label(player.lang("gmne.debug.average_ping", player.getAveragePing())));
        menu.add(new Label(player.lang("gmne.debug.average_ping_ticks", (player.getAveragePing() / 50))));

        menu.add(new Label(player.lang("gmne.debug.last_ping", player.getLastPing())));
        menu.add(new Label(player.lang("gmne.debug.ping_sample", player.getPingSample())));
        menu.add(new Label(player.lang("gmne.debug.ping_sample_size", player.getPingSampleSize())));

        menu.add(new SubSectionLabel(player.lang("gmne.debug.plugin_info")));
        menu.add(new Label(player.lang("gmne.debug.version", PluginVersion.GE_VERSION)));
        menu.add(new Label(player.lang("gmne.debug.server_type", ServerType.get())));
        menu.add(new Label(player.lang("gmne.debug.floodgate_installed", player.lang(IsAvailable.floodgate() ? "gmne.console.yes" : "gmne.console.no"))));

    }

    @Override
    public List<String> getHeader(ExtrasPlayer player) {
        return List.of(BedrockLocale.OPTIONS.DEBUG,
                player.lang("gmne.settings.debug.header"));
    }

    @Override
    public FormImage getImage() {
        return FormImage.of(FormImage.Type.PATH, "textures/ui/ChainSquare.png");
    }
}
