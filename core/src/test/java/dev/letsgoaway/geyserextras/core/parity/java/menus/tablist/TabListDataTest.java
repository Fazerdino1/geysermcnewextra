package dev.letsgoaway.geyserextras.core.parity.java.menus.tablist;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabListDataTest {
    private final TabListData data = new TabListData(null);

    private static PlayerListEntry entry(UUID id, String name, boolean listed, int latency) {
        PlayerListEntry entry = new PlayerListEntry(id);
        if (name != null) {
            entry.setProfile(new GameProfile(id, name));
        }
        entry.setListed(listed);
        entry.setLatency(latency);
        return entry;
    }

    private void add(PlayerListEntry... entries) {
        data.apply(EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_LISTED, PlayerListEntryAction.UPDATE_LATENCY), entries);
    }

    @Test
    void addsAndUpdatesEntries() {
        UUID alice = UUID.randomUUID();
        add(entry(alice, "Alice", true, 10));
        data.apply(EnumSet.of(PlayerListEntryAction.UPDATE_LATENCY), new PlayerListEntry[]{entry(alice, null, false, 250)});

        List<PlayerListEntry> listed = data.listedEntries();
        assertEquals(1, listed.size());
        assertEquals(250, listed.get(0).getLatency());
        assertEquals("Alice", listed.get(0).getProfile().getName(), "an update must not wipe the profile");
        assertTrue(listed.get(0).isListed(), "UPDATE_LATENCY must not change the listed flag");
    }

    @Test
    void removedPlayersLeaveTheList() {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        add(entry(alice, "Alice", true, 0), entry(bob, "Bob", true, 0));
        data.remove(List.of(alice));
        assertEquals(List.of(bob), data.listedEntries().stream().map(PlayerListEntry::getProfileId).toList());
    }

    @Test
    void unlistedPlayersAreKeptButHidden() {
        UUID npc = UUID.randomUUID();
        add(entry(npc, "NPC", false, 0));
        assertTrue(data.listedEntries().isEmpty());
        data.apply(EnumSet.of(PlayerListEntryAction.UPDATE_LISTED), new PlayerListEntry[]{entry(npc, null, true, 0)});
        assertEquals(1, data.listedEntries().size());
    }

    @Test
    void entryWithoutProfileHasAName() {
        UUID ghost = UUID.randomUUID();
        // UPDATE_LISTED without ADD_PLAYER: no profile, no display name (opening the menu used to throw)
        data.apply(EnumSet.of(PlayerListEntryAction.UPDATE_LISTED), new PlayerListEntry[]{entry(ghost, null, true, 0)});
        assertTrue(TabListData.getPlayerListName(data.listedEntries().get(0)).endsWith(ghost.toString()));
    }

    @Test
    void sortsByListOrderLikeJava() {
        UUID low = UUID.randomUUID();
        UUID high = UUID.randomUUID();
        add(entry(low, "Low", true, 0), entry(high, "High", true, 0));
        PlayerListEntry order = new PlayerListEntry(high);
        order.setListOrder(10);
        data.apply(EnumSet.of(PlayerListEntryAction.UPDATE_LIST_ORDER), new PlayerListEntry[]{order});
        assertEquals(high, data.listedEntries().get(0).getProfileId());
    }
}
