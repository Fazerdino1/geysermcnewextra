package fr.crossplaymc.geysermcnewextra.core.menus.settings.menus;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockModal;
import fr.crossplaymc.geysermcnewextra.core.locale.BedrockLocale;
import org.geysermc.cumulus.form.ModalForm;

public class ResetModal extends BedrockModal {
    @Override

    public ModalForm create(ExtrasPlayer player) {
        setContent(player.translateGE("ge.settings.reset.reconnect")
                + "\n\n" +
                player.translate("mco.configure.world.leave.question.line2"));

        setYesText(BedrockLocale.CONTROLLER.RESET_TO_DEFAULT);
        setNoText(BedrockLocale.CONTROLLER.CANCEL);

        return super.create(player);
    }

    @Override
    public void onSubmit(ExtrasPlayer player, boolean accepted) {
        if (accepted) {
            new Thread(() -> {
                if (player.getUserPrefs().delete()) {
                    player.reconnect();
                }
            }).start();
        } else {
            onClose(player);
        }
    }

    @Override
    public void onClose(ExtrasPlayer player) {
        player.sendForm(new SettingsMenu());
    }
}
