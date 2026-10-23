package fr.crossplaymc.geysermcnewextra.core.menus.settings.sections;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.parity.bedrock.EmoteUtils;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Action;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.EmoteBindings;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Remappable;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Dropdown;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Label;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Toggle;
import fr.crossplaymc.geysermcnewextra.core.locale.BedrockLocale;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class BindingsSection extends Section {
    @Override
    public void build(BedrockForm menu, GeyserSession session, ExtrasPlayer player) {
        LinkedHashMap<String, Action> actionNames = new LinkedHashMap<>();
        for (Action action : Action.values()) {
            actionNames.put(action.translate(player), action);
        }
        for (Remappable binding : Remappable.values()) {
            if (binding.equals(Remappable.SNEAK_DROP)) {
                continue; // todo: implement sneak drop
            }
            menu.add(new Dropdown(binding.translate(player),
                    new ArrayList<>(actionNames.keySet()),
                    player.getPreferences().getAction(binding).translate(player), (str) -> {
                player.getPreferences().setAction(binding, actionNames.get(str));
            }));
            if (EmoteBindings.isEmoteBinding(binding)) {
                addEmoteLink(menu, player, binding);
            }
        }
    }

    private static void addEmoteLink(BedrockForm menu, ExtrasPlayer player, Remappable binding) {
        UUID linked = player.getPreferences().emoteLinks.get(binding);
        String linkedName = linked == null ? null : EmoteUtils.getName(linked);
        if (linked != null && linkedName == null) {
            linkedName = linked.toString();
        }
        menu.add(new Label(String.format(player.translateGE("gmne.emote_link.current"),
                linkedName != null ? linkedName : player.translateGE("gmne.emote_link.none"))));
        menu.add(new Toggle(String.format(player.translateGE("gmne.emote_link.toggle"), binding.translate(player)), false, (link) -> {
            if (link) {
                player.queueEmoteLink(binding);
            }
        }));
    }

    @Override
    public List<String> getHeader(ExtrasPlayer player) {
        return List.of(BedrockLocale.CONTROLLER.BINDINGS, "");
    }

    @Override
    public FormImage getImage() {
        return FormImage.of(FormImage.Type.PATH, "textures/ui/controller_glyph_color.png");
    }
}
