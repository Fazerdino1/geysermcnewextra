package fr.crossplaymc.geysermcnewextra.core.preferences.bindings;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmoteBindingsTest {
    private final UUID wave = UUID.fromString("4c8ae710-df2e-47cd-814d-cc7bf21a3d67");
    private final UUID clap = UUID.fromString("9a469a61-c83b-4ba9-b507-bdbe64430582");

    @Test
    void findsTheLinkedBinding() {
        Map<Remappable, UUID> links = new HashMap<>();
        EmoteBindings.link(links, Remappable.EMOTE_2, wave);
        assertEquals(Remappable.EMOTE_2, EmoteBindings.find(links, wave));
        assertNull(EmoteBindings.find(links, clap));
    }

    @Test
    void anEmoteTriggersOnlyOneBinding() {
        Map<Remappable, UUID> links = new HashMap<>();
        EmoteBindings.link(links, Remappable.EMOTE_1, wave);
        EmoteBindings.link(links, Remappable.EMOTE_3, wave);
        assertEquals(Remappable.EMOTE_3, EmoteBindings.find(links, wave));
        assertNull(links.get(Remappable.EMOTE_1));
    }

    @Test
    void relinkingABindingReplacesItsEmote() {
        Map<Remappable, UUID> links = new HashMap<>();
        EmoteBindings.link(links, Remappable.EMOTE_1, wave);
        EmoteBindings.link(links, Remappable.EMOTE_1, clap);
        assertNull(EmoteBindings.find(links, wave));
        assertEquals(Remappable.EMOTE_1, EmoteBindings.find(links, clap));
    }

    @Test
    void rejectsNonEmoteBindings() {
        assertThrows(IllegalArgumentException.class, () -> EmoteBindings.link(new HashMap<>(), Remappable.PICK_BLOCK, wave));
    }
}
