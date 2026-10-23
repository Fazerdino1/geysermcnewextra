package fr.crossplaymc.geysermcnewextra.core.injectors.bedrock;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Remappable;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import org.cloudburstmc.protocol.bedrock.packet.ServerSettingsRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerSettingsResponsePacket;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.impl.FormDefinitions;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.bedrock.BedrockServerSettingsRequestTranslator;

import java.util.concurrent.TimeUnit;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class BedrockServerSettingsRequestInjector extends BedrockServerSettingsRequestTranslator {
    private static volatile boolean settingsFormSupported = true;

    @Override
    public void translate(GeyserSession session, ServerSettingsRequestPacket packet) {
        if (!GE.getConfig().isEnableMenu()) {
            super.translate(session, packet);
            return;
        }

        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player == null) {
            super.translate(session, packet);
            return;
        }
        // With Floodgate installed, the form classes can come from Floodgate's copy of Cumulus, which
        // Geyser's form cache then rejects with a LinkageError. Whether that happens depends on the
        // platform's class loading, so try once and fall back to the toast if it does.
        if (IsAvailable.floodgate() && !settingsFormSupported) {
            sendFallback(session, packet, player);
            return;
        }

        CustomForm form;
        int formId;
        String jsonData;
        try {
            form = player.getPreferences().getSettingsMenuForm().open(player);
            formId = session.getFormCache().addForm(form);
            jsonData = FormDefinitions.instance().codecFor(form).jsonData(form);
        } catch (LinkageError e) {
            settingsFormSupported = false;
            SERVER.warn("GeyserMCNewExtra settings cannot be shown in the Bedrock settings screen on this server ("
                    + e + "); players will be told to double tap inventory instead.");
            sendFallback(session, packet, player);
            return;
        }
        player.getPreferences().runAction(Remappable.SETTINGS);

        // Fixes https://bugs.mojang.com/browse/MCPE-94012 because of the delay
        session.scheduleInEventLoop(() -> {
            ServerSettingsResponsePacket serverSettingsResponsePacket = new ServerSettingsResponsePacket();
            serverSettingsResponsePacket.setFormData(jsonData);
            serverSettingsResponsePacket.setFormId(formId);
            session.sendUpstreamPacket(serverSettingsResponsePacket);
        }, 1, TimeUnit.SECONDS);
    }

    private void sendFallback(GeyserSession session, ServerSettingsRequestPacket packet, ExtrasPlayer player) {
        player.sendSystemToast(player.translateGE("ge.form_error_toast.line1"), player.translateGE("ge.form_error_toast.line2"));
        super.translate(session, packet);
    }
}
