package fr.crossplaymc.geysermcnewextra.core.features.recipemenus;

/**
 * Where the slots of a server-side menu appear in the 27-slot chest shown to the Bedrock player, and back.
 * The player's inventory (36 slots) follows the menu slots on the server and the chest slots on the client.
 */
public enum RecipeMenuLayout {
    // Left item, right item, result
    ANVIL(new int[]{10, 12, 15}),
    // Template, base item, material, result
    SMITHING(new int[]{10, 12, 14, 16});

    public static final int CHEST_SIZE = 27;
    public static final int PLAYER_SLOTS = 36;
    /**
     * A slot that exists on one side only: a decoration slot of the chest, or an unknown slot.
     */
    public static final int NONE = -2;
    /**
     * The chest slot showing the anvil cost or the smithing table help.
     */
    public static final int INFO_SLOT = 22;

    private final int[] chestSlots;

    RecipeMenuLayout(int[] chestSlots) {
        this.chestSlots = chestSlots;
    }

    public int menuSlots() {
        return chestSlots.length;
    }

    public int resultSlot() {
        return chestSlots.length - 1;
    }

    /**
     * @param serverSlot a slot of the server menu; -1 (cursor) and -999 (outside) are kept
     */
    public int toChest(int serverSlot) {
        if (serverSlot < 0) {
            return serverSlot;
        }
        if (serverSlot < chestSlots.length) {
            return chestSlots[serverSlot];
        }
        if (serverSlot < chestSlots.length + PLAYER_SLOTS) {
            return serverSlot - chestSlots.length + CHEST_SIZE;
        }
        return NONE;
    }

    /**
     * @param chestSlot a slot of the chest shown to the player; -1 (cursor) and -999 (outside) are kept
     * @return the server slot, or {@link #NONE} for a decoration slot
     */
    public int toServer(int chestSlot) {
        if (chestSlot < 0) {
            return chestSlot;
        }
        if (chestSlot < CHEST_SIZE) {
            for (int i = 0; i < chestSlots.length; i++) {
                if (chestSlots[i] == chestSlot) {
                    return i;
                }
            }
            return NONE;
        }
        if (chestSlot < CHEST_SIZE + PLAYER_SLOTS) {
            return chestSlot - CHEST_SIZE + chestSlots.length;
        }
        return NONE;
    }

    /**
     * @return the chest contents for the server menu contents, decoration slots filled by {@code decoration}
     */
    public <T> T[] toChestContents(T[] serverItems, T[] chest, java.util.function.IntFunction<T> decoration) {
        for (int slot = 0; slot < CHEST_SIZE; slot++) {
            chest[slot] = toServer(slot) == NONE ? decoration.apply(slot) : null;
        }
        for (int slot = 0; slot < serverItems.length; slot++) {
            int target = toChest(slot);
            if (target >= 0 && target < chest.length) {
                chest[target] = serverItems[slot];
            }
        }
        return chest;
    }
}
