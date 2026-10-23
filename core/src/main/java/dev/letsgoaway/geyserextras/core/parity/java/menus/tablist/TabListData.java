package dev.letsgoaway.geyserextras.core.parity.java.menus.tablist;

import dev.letsgoaway.geyserextras.core.ExtrasPlayer;
import dev.letsgoaway.geyserextras.core.utils.IdUtils;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.geyser.text.ChatColor;
import org.geysermc.geyser.translator.text.MessageTranslator;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.letsgoaway.geyserextras.core.GeyserExtras.SERVER;

public class TabListData {
    private final ExtrasPlayer player;
    // Every known entry, listed or not, like the Java client keeps it. Written by packet
    // translation and read when building forms, so access is synchronized on the map.
    private final LinkedHashMap<UUID, PlayerListEntry> players;
    @Setter
    @Getter
    private String header = " ";
    @Setter
    @Getter
    private String footer = " ";

    private static volatile boolean trackingAvailable = true;

    public static boolean isTrackingAvailable() {
        return trackingAvailable;
    }

    /**
     * Turns tab list tracking off for this run after an incompatibility, with a single warning
     * instead of a "Could not translate packet" error for every player list packet.
     */
    public static void disableTracking(Throwable cause) {
        if (trackingAvailable) {
            trackingAvailable = false;
            SERVER.warn("Player list menu disabled: this GeyserExtras jar does not match this Geyser build (" + cause
                    + "). On Paper/Spigot use GeyserExtras-CPMC-Spigot.jar in plugins/, not the extension jar.");
        }
    }

    public TabListData(ExtrasPlayer player) {
        this.player = player;
        players = new LinkedHashMap<>();
    }

    /**
     * Applies a ClientboundPlayerInfoUpdatePacket: ADD_PLAYER (re)creates entries, the other actions
     * update the fields they carry, like the Java client.
     */
    public void apply(Set<PlayerListEntryAction> actions, PlayerListEntry[] entries) {
        synchronized (players) {
            for (PlayerListEntry entry : entries) {
                PlayerListEntry cached = players.get(entry.getProfileId());
                if (cached == null || actions.contains(PlayerListEntryAction.ADD_PLAYER)) {
                    cached = new PlayerListEntry(entry.getProfileId());
                    cached.setProfile(entry.getProfile());
                    players.put(entry.getProfileId(), cached);
                }
                if (actions.contains(PlayerListEntryAction.UPDATE_LISTED)) {
                    cached.setListed(entry.isListed());
                }
                if (actions.contains(PlayerListEntryAction.UPDATE_DISPLAY_NAME)) {
                    cached.setDisplayName(entry.getDisplayName());
                }
                if (actions.contains(PlayerListEntryAction.UPDATE_LATENCY)) {
                    cached.setLatency(entry.getLatency());
                }
                if (actions.contains(PlayerListEntryAction.UPDATE_GAME_MODE)) {
                    cached.setGameMode(entry.getGameMode());
                }
                if (actions.contains(PlayerListEntryAction.UPDATE_LIST_ORDER)) {
                    cached.setListOrder(entry.getListOrder());
                }
            }
        }
    }

    /**
     * Applies a ClientboundPlayerInfoRemovePacket (also sent by proxies when switching servers).
     */
    public void remove(Iterable<UUID> profileIds) {
        synchronized (players) {
            for (UUID id : profileIds) {
                players.remove(id);
            }
        }
    }

    public void clear() {
        synchronized (players) {
            players.clear();
        }
    }

    /**
     * @return a copy of the listed entries in Java tab list order (list order, then insertion order)
     */
    public List<PlayerListEntry> listedEntries() {
        synchronized (players) {
            List<PlayerListEntry> listed = new ArrayList<>();
            for (PlayerListEntry entry : players.values()) {
                if (entry.isListed()) {
                    listed.add(entry);
                }
            }
            // Higher list order first, as on Java; the sort is stable for equal orders
            listed.sort(Comparator.comparingInt(PlayerListEntry::getListOrder).reversed());
            return listed;
        }
    }

    public static String getPlayerListName(PlayerListEntry entry) {
        if (entry.getDisplayName() != null) {
            return MessageTranslator.convertMessage(entry.getDisplayName());
        }
        if (entry.getProfile() != null && entry.getProfile().getName() != null) {
            return ChatColor.DARK_GRAY + entry.getProfile().getName();
        }
        // Entries created by an update without ADD_PLAYER have neither (opening the list used to throw)
        return ChatColor.DARK_GRAY + entry.getProfileId().toString();
    }

    public static String getPlayerListHead(PlayerListEntry entry) {
        return "https://starlightskins.lunareclipse.studio/render/pixel/" + getPlayerListID(entry.getProfileId()) + "/face?cameraWidth=8";
    }


    private static String getPlayerListID(UUID profileID) {
        long XUID = IdUtils.getBedrockXUID(profileID);
        if (XUID != -1) {
            // StarlightStudios needs a dot infront of it to recognize it as a XUID.
            return "." + XUID;
        }
        // If its not a Bedrock Player, then we can just return the UUID as a string.
        return profileID.toString();
    }

    // Ping icons are supplied by GeyserExtrasPack
    // Matches how Minecraft Java Edition chooses the icon to use
    public static String getPingIcon(PlayerListEntry entry) {
        String pingIcon = "\uF834";
        int ping = entry.getLatency();
        if (ping < 0) {
            pingIcon = "\uF835";
        } else if (ping < 150) {
            pingIcon = "\uF830";
        } else if (ping < 300) {
            pingIcon = "\uF831";
        } else if (ping < 600) {
            pingIcon = "\uF832";
        } else if (ping < 1000) {
            pingIcon = "\uF833";
        }
        return pingIcon;
    }
}
