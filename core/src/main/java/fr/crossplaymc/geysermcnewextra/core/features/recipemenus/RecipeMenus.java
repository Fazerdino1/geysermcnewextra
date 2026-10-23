package fr.crossplaymc.geysermcnewextra.core.features.recipemenus;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.config.GeyserMCNewExtraConfig;
import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.inventory.JavaContainerSetSlotTranslator;
import org.geysermc.mcprotocollib.network.event.session.PacketSendingEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ClickItemAction;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerActionType;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.HashedStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

/**
 * Anvils and smithing tables shown to a Bedrock player as a chest whose result slot is filled by the server.
 * <p>
 * The Bedrock anvil and smithing table compute their result on the client, so results made by plugins and
 * datapacks (custom recipes, custom enchantments) never show up. Here the server keeps its real menu and
 * computes everything; only what Geyser sees changes: the menu is announced as a chest, the slot numbers
 * of the server packets are moved to chest slots, and the slots of the player's clicks are moved back
 * before they reach the server. Clicks on decoration slots become a click that does nothing and makes the
 * server send the whole menu again, which also undoes what the client predicted.
 * <p>
 * Written from scratch for GeyserMCNewExtra; replaces the "Geyser Recipe Fix" plugin (no license).
 */
public final class RecipeMenus extends SessionAdapter {
    // Cost of the anvil (ContainerProperty REPAIR_COST of the anvil)
    private static final int ANVIL_COST_PROPERTY = 0;
    // Above this cost the vanilla anvil refuses the repair, except in creative mode
    private static final int TOO_EXPENSIVE = 40;
    // A state id the server never uses: the server answers by sending the whole menu again
    private static final int RESYNC_STATE_ID = -1;

    private static final JavaContainerSetSlotTranslator SET_SLOT = new JavaContainerSetSlotTranslator();
    private static volatile boolean disabled = false;

    private final ExtrasPlayer player;

    private volatile OpenMenu open;

    private record OpenMenu(int containerId, RecipeMenuLayout layout, int[] state) {
        // state[0]: last state id from the server, state[1]: anvil cost
    }

    public RecipeMenus(ExtrasPlayer player) {
        this.player = player;
    }

    /**
     * Starts watching the clicks this player's Geyser sends to the server.
     */
    public void attach(GeyserSession session) {
        if (disabled || !GE.getConfig().getRecipeMenus().isEnabled()) {
            return;
        }
        try {
            session.getDownstream().getSession().addListener(this);
        } catch (RuntimeException | LinkageError e) {
            disable(e);
        }
    }

    /**
     * Turns the feature off for this run after an incompatibility with Geyser, with a single warning.
     */
    public static void disable(Throwable cause) {
        if (!disabled) {
            disabled = true;
            SERVER.warn(Lang.console("gmne.console.recipe_menus_disabled", cause));
        }
    }

    public static boolean isDisabled() {
        return disabled;
    }

    /**
     * @return the layout to show instead of this menu, or null to let Geyser show the Bedrock menu
     */
    @Nullable
    public RecipeMenuLayout layoutFor(GeyserSession session, ContainerType type) {
        GeyserMCNewExtraConfig.RecipeMenus settings = GE.getConfig().getRecipeMenus();
        if (disabled || !settings.isEnabled() || !settings.appliesToWorld(player.getWorldName())) {
            return null;
        }
        // Sneaking with empty hands opens the normal menu, e.g. to rename an item in the anvil
        if (session.isSneaking()) {
            return null;
        }
        if (type == ContainerType.ANVIL && settings.isAnvil()) {
            return RecipeMenuLayout.ANVIL;
        }
        if (type == ContainerType.SMITHING && settings.isSmithingTable()) {
            return RecipeMenuLayout.SMITHING;
        }
        return null;
    }

    public void opened(int containerId, RecipeMenuLayout layout) {
        open = new OpenMenu(containerId, layout, new int[]{0, 0});
        SERVER.debug("Recipe menu " + layout + " opened as a chest, container " + containerId + " for " + player.getBedrockXUID());
    }

    public void closed() {
        open = null;
    }

    /**
     * @return the layout of the menu if {@code containerId} is the open recipe menu
     */
    @Nullable
    public RecipeMenuLayout layoutOf(int containerId) {
        OpenMenu menu = open;
        return menu != null && menu.containerId() == containerId ? menu.layout() : null;
    }

    public void stateId(int stateId) {
        OpenMenu menu = open;
        if (menu != null) {
            menu.state()[0] = stateId;
        }
    }

    public ItemStack[] toChestContents(GeyserSession session, RecipeMenuLayout layout, ItemStack[] serverItems) {
        ItemStack[] chest = new ItemStack[RecipeMenuLayout.CHEST_SIZE + RecipeMenuLayout.PLAYER_SLOTS];
        return layout.toChestContents(serverItems, chest, slot -> slot == RecipeMenuLayout.INFO_SLOT ? infoItem(session, layout) : filler());
    }

    /**
     * The anvil cost is shown on the info item, the chest has no place for it.
     *
     * @return whether the property was the anvil cost (the packet is then handled)
     */
    public boolean onProperty(GeyserSession session, int containerId, int property, int value) {
        OpenMenu menu = open;
        if (menu == null || menu.containerId() != containerId || menu.layout() != RecipeMenuLayout.ANVIL) {
            return false;
        }
        if (property == ANVIL_COST_PROPERTY) {
            menu.state()[1] = value;
            SET_SLOT.translate(session, new ClientboundContainerSetSlotPacket(containerId, menu.state()[0],
                    RecipeMenuLayout.INFO_SLOT, infoItem(session, menu.layout())));
        }
        // Other anvil properties mean nothing for a chest
        return true;
    }

    private ItemStack infoItem(GeyserSession session, RecipeMenuLayout layout) {
        String locale = session.locale();
        if (layout == RecipeMenuLayout.SMITHING) {
            return named(Items.SMITHING_TABLE, Lang.get("gmne.recipe_menus.smithing_info", locale),
                    Lang.get("gmne.recipe_menus.smithing_hint", locale));
        }
        OpenMenu menu = open;
        int cost = menu == null ? 0 : menu.state()[1];
        String hint = Lang.get("gmne.recipe_menus.anvil_hint", locale);
        if (cost <= 0) {
            return named(Items.ANVIL, Lang.get("gmne.recipe_menus.anvil_info", locale), hint);
        }
        if (cost >= TOO_EXPENSIVE && session.getGameMode() != GameMode.CREATIVE) {
            return named(Items.BARRIER, Lang.get("gmne.recipe_menus.too_expensive", locale), hint);
        }
        return named(Items.EXPERIENCE_BOTTLE, Lang.get("gmne.recipe_menus.cost", locale, cost), hint);
    }

    private static ItemStack filler() {
        return named(Items.GRAY_STAINED_GLASS_PANE, " ", null);
    }

    private static ItemStack named(Item item, String name, @Nullable String lore) {
        DataComponents components = new DataComponents(new HashMap<>());
        components.put(DataComponentTypes.CUSTOM_NAME, Component.text(name));
        if (lore != null) {
            components.put(DataComponentTypes.LORE, List.of(Component.text(lore)));
        }
        return new ItemStack(item.javaId(), 1, components);
    }

    // Packets from Geyser to the server: move the slots of clicks back to the server menu
    @Override
    public void packetSending(PacketSendingEvent event) {
        OpenMenu menu = open;
        if (menu == null) {
            return;
        }
        try {
            if (event.getPacket() instanceof ServerboundContainerClickPacket click && click.getContainerId() == menu.containerId()) {
                event.setPacket(toServer(click, menu.layout()));
            } else if (event.getPacket() instanceof ServerboundContainerClosePacket close && close.getContainerId() == menu.containerId()) {
                closed();
            }
        } catch (RuntimeException | LinkageError e) {
            // Better a wrong click the server corrects than a click on the wrong slot: ask for a resync
            closed();
            disable(e);
        }
    }

    static ServerboundContainerClickPacket toServer(ServerboundContainerClickPacket click, RecipeMenuLayout layout) {
        int slot = layout.toServer(click.getSlot());
        if (slot == RecipeMenuLayout.NONE) {
            // A decoration was clicked: do nothing on the server, and let it send the real menu back
            return click.withSlot(-1)
                    .withAction(ContainerActionType.CLICK_ITEM)
                    .withParam(ClickItemAction.LEFT_CLICK)
                    .withStateId(RESYNC_STATE_ID)
                    .withChangedSlots(new Int2ObjectOpenHashMap<>());
        }
        Int2ObjectMap<HashedStack> changed = new Int2ObjectOpenHashMap<>();
        boolean predictedDecoration = false;
        for (Int2ObjectMap.Entry<HashedStack> entry : click.getChangedSlots().int2ObjectEntrySet()) {
            int target = layout.toServer(entry.getIntKey());
            if (target == RecipeMenuLayout.NONE) {
                predictedDecoration = true;
            } else {
                changed.put(target, entry.getValue());
            }
        }
        ServerboundContainerClickPacket moved = click.withSlot(slot).withChangedSlots(changed);
        // The client thinks a decoration moved: make sure the server sends the real menu back
        return predictedDecoration ? moved.withStateId(RESYNC_STATE_ID) : moved;
    }
}
