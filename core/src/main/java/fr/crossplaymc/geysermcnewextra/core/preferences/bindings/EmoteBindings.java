package fr.crossplaymc.geysermcnewextra.core.preferences.bindings;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Links the EMOTE_1..4 bindings to specific emotes.
 * Geyser 2.11+ no longer tells us which wheel slot an emote is in, so each binding remembers
 * the emote the player recorded for it instead. An emote triggers at most one binding.
 */
public final class EmoteBindings {
    public static final List<Remappable> BINDINGS = List.of(Remappable.EMOTE_1, Remappable.EMOTE_2, Remappable.EMOTE_3, Remappable.EMOTE_4);

    private EmoteBindings() {
    }

    public static boolean isEmoteBinding(Remappable binding) {
        return BINDINGS.contains(binding);
    }

    public static void link(Map<Remappable, UUID> links, Remappable binding, UUID emote) {
        if (!isEmoteBinding(binding)) {
            throw new IllegalArgumentException(binding + " is not an emote binding");
        }
        links.values().removeIf(emote::equals);
        links.put(binding, emote);
    }

    @Nullable
    public static Remappable find(Map<Remappable, UUID> links, UUID emote) {
        for (Remappable binding : BINDINGS) {
            if (Objects.equals(links.get(binding), emote)) {
                return binding;
            }
        }
        return null;
    }
}
