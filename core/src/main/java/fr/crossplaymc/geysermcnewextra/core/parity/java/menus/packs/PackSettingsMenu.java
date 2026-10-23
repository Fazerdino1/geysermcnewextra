package fr.crossplaymc.geysermcnewextra.core.parity.java.menus.packs;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Label;
import fr.crossplaymc.geysermcnewextra.core.form.elements.StepSlider;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.api.pack.ResourcePackManifest;

import java.util.ArrayList;
import java.util.UUID;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class PackSettingsMenu extends BedrockForm {
    private final ResourcePack pack;
    private final UUID packID;

    public PackSettingsMenu(ResourcePack pack) {
        super();
        this.pack = pack;
        this.packID = pack.manifest().header().uuid();
    }

    private static ArrayList<String> getSubpackNames(ResourcePack pack) {
        ArrayList<String> subpackNames = new ArrayList<>();
        for (ResourcePackManifest.Subpack subpack : pack.manifest().subpacks()) {
            subpackNames.add(subpack.name());
        }
        return subpackNames;
    }


    @Override
    public CustomForm.Builder create(ExtrasPlayer player) {
        setTitle(player.lang("gmne.packs.settings_title", this.pack.manifest().header().name()));
        ArrayList<String> packNames = getSubpackNames(pack);

        String defaultPack = packNames.get(packNames.size() - 1);
        if (player.getPreferences().getSelectedSubpacks().containsKey(packID)){
            String pref = player.getPreferences().getSelectedSubpacks().get(packID);
            defaultPack = packNames.contains(pref) ? pref : defaultPack;
        }

        add(new StepSlider(player.lang("gmne.packs.resolution"), packNames, defaultPack, (str) -> {
            player.getPreferences().getSelectedSubpacks().put(packID, str);
        }));
        for (ResourcePackManifest.Setting setting : pack.manifest().settings()) {
            if (setting.type().equals("label")) {
                add(new Label(setting.text()));
            }
        }

        return super.create(player);
    }

    @Override
    public void onSubmit(ExtrasPlayer player) {
        super.onSubmit(player);
        player.setPacksUpdated(true);
        player.sendForm(new PackMenu());
    }

    @Override
    public void onClose(ExtrasPlayer player) {
        super.onClose(player);
        player.sendForm(new PackManagerMenu(pack));
    }
}
