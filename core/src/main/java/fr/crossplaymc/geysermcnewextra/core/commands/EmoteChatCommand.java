package fr.crossplaymc.geysermcnewextra.core.commands;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.locale.GELocale;
import fr.crossplaymc.geysermcnewextra.core.preferences.JavaPreferencesData;

import java.util.List;
import java.util.UUID;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;


public class EmoteChatCommand implements BedrockCommand {

    // dont do anything bc this is configured in the chat settings on bedrock edition
    // and geysermcnewextra doesnt send a message anyway
    @Override
    public void onExecute(ExtrasPlayer player, List<String> args, String label) {
        return;
    }

    @Override
    public void onJavaExecute(UUID playerUUID, List<String> args, String label) {
        JavaPreferencesData data = GE.getJavaPreferencesData(playerUUID);
        if (label.equalsIgnoreCase("unmuteemotechat")) {
            data.muteEmoteChat = false;
        } else if (label.equalsIgnoreCase("muteemotechat")) {
            data.muteEmoteChat = true;
        } else {
            data.muteEmoteChat = !data.muteEmoteChat;
        }
        data.save();
        SERVER.sendRawMessage(playerUUID, GELocale.translate("ge.settings.java.emotechat." + (data.muteEmoteChat ? "muted" : "unmuted"), GE.getConfig().getDefaultLocale()));
    }
}
