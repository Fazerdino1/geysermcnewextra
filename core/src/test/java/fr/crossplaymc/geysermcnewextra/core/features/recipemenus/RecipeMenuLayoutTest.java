package fr.crossplaymc.geysermcnewextra.core.features.recipemenus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeMenuLayoutTest {
    @Test
    void everyServerSlotGoesThereAndBack() {
        for (RecipeMenuLayout layout : RecipeMenuLayout.values()) {
            for (int slot = 0; slot < layout.menuSlots() + RecipeMenuLayout.PLAYER_SLOTS; slot++) {
                int chest = layout.toChest(slot);
                assertEquals(slot, layout.toServer(chest), layout + " slot " + slot);
            }
        }
    }

    @Test
    void playerInventoryFollowsTheChest() {
        // First hotbar slot: after the 3 anvil slots on the server, after the 27 chest slots on the client
        assertEquals(27 + 27, RecipeMenuLayout.ANVIL.toChest(3 + 27));
        assertEquals(4, RecipeMenuLayout.SMITHING.toServer(27));
        assertEquals(39, RecipeMenuLayout.SMITHING.toServer(62));
    }

    @Test
    void decorationsAndUnknownSlotsHaveNoServerSlot() {
        assertEquals(RecipeMenuLayout.NONE, RecipeMenuLayout.ANVIL.toServer(0));
        assertEquals(RecipeMenuLayout.NONE, RecipeMenuLayout.ANVIL.toServer(RecipeMenuLayout.INFO_SLOT));
        assertEquals(RecipeMenuLayout.NONE, RecipeMenuLayout.ANVIL.toServer(63));
        assertEquals(RecipeMenuLayout.NONE, RecipeMenuLayout.ANVIL.toChest(39));
    }

    @Test
    void cursorAndOutsideClicksAreKept() {
        assertEquals(-999, RecipeMenuLayout.ANVIL.toServer(-999));
        assertEquals(-1, RecipeMenuLayout.SMITHING.toChest(-1));
    }

    @Test
    void resultIsTheLastMenuSlot() {
        assertEquals(2, RecipeMenuLayout.ANVIL.resultSlot());
        assertEquals(3, RecipeMenuLayout.SMITHING.resultSlot());
    }

    @Test
    void contentsAreMovedIntoTheChest() {
        String[] server = new String[3 + 36];
        server[0] = "sword";
        server[2] = "result";
        server[3] = "first inventory slot";
        String[] chest = RecipeMenuLayout.ANVIL.toChestContents(server, new String[63], slot -> "pane");
        assertEquals("sword", chest[10]);
        assertEquals("result", chest[15]);
        assertEquals("first inventory slot", chest[27]);
        assertEquals("pane", chest[0]);
        assertEquals(null, chest[12]);
        assertArrayEquals(new String[]{"pane", "pane"}, new String[]{chest[11], chest[26]});
    }
}
