package fr.crossplaymc.geysermcnewextra.core.features.recipemenus;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ClickItemAction;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerActionType;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ShiftClickItemAction;
import org.geysermc.mcprotocollib.protocol.data.game.item.HashedStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeMenuClickTest {
    private static final HashedStack SWORD = new HashedStack(1, 1, Map.of(), Set.of());

    private static ServerboundContainerClickPacket click(int slot, Map<Integer, HashedStack> changed) {
        return new ServerboundContainerClickPacket(5, 12, slot, ContainerActionType.CLICK_ITEM, ClickItemAction.LEFT_CLICK,
                null, new Int2ObjectOpenHashMap<>(changed));
    }

    @Test
    void clickOnTheResultReachesTheAnvilResult() {
        ServerboundContainerClickPacket moved = RecipeMenus.toServer(click(15, Map.of(15, SWORD)), RecipeMenuLayout.ANVIL);
        assertEquals(2, moved.getSlot());
        assertEquals(12, moved.getStateId());
        assertEquals(Map.of(2, SWORD), Map.copyOf(moved.getChangedSlots()));
    }

    @Test
    void clickInTheInventoryKeepsItsSlot() {
        // Chest 27 = first slot of the player's inventory = anvil slot 3
        ServerboundContainerClickPacket moved = RecipeMenus.toServer(click(27, Map.of(27, SWORD, 10, SWORD)), RecipeMenuLayout.ANVIL);
        assertEquals(3, moved.getSlot());
        assertEquals(Map.of(3, SWORD, 0, SWORD), Map.copyOf(moved.getChangedSlots()));
    }

    @Test
    void clickOnADecorationDoesNothingAndAsksForTheRealMenu() {
        ServerboundContainerClickPacket moved = RecipeMenus.toServer(click(0, Map.of(0, SWORD)), RecipeMenuLayout.SMITHING);
        assertEquals(-1, moved.getSlot());
        assertEquals(-1, moved.getStateId());
        assertEquals(ContainerActionType.CLICK_ITEM, moved.getAction());
        assertTrue(moved.getChangedSlots().isEmpty());
    }

    @Test
    void predictedDecorationChangeAsksForTheRealMenu() {
        // Shift-click from the inventory that the client thinks lands on a decoration slot
        ServerboundContainerClickPacket shift = new ServerboundContainerClickPacket(5, 12, 30, ContainerActionType.SHIFT_CLICK_ITEM,
                ShiftClickItemAction.LEFT_CLICK, null, new Int2ObjectOpenHashMap<>(Map.of(30, SWORD, 9, SWORD)));
        ServerboundContainerClickPacket moved = RecipeMenus.toServer(shift, RecipeMenuLayout.ANVIL);
        assertEquals(6, moved.getSlot());
        assertEquals(-1, moved.getStateId());
        assertEquals(Map.of(6, SWORD), Map.copyOf(moved.getChangedSlots()));
    }

    @Test
    void outsideClickIsKept() {
        assertEquals(-999, RecipeMenus.toServer(click(-999, Map.of()), RecipeMenuLayout.ANVIL).getSlot());
    }
}
