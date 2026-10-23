package fr.crossplaymc.geysermcnewextra.core.injectors;

import fr.crossplaymc.geysermcnewextra.ServerType;
import fr.crossplaymc.geysermcnewextra.core.parity.java.combat.SoundReplacer;
import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.injectors.bedrock.*;
import fr.crossplaymc.geysermcnewextra.core.injectors.java.*;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundServerLinksPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSoundEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundTabListPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundTickingStatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundEntityEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSoundPacket;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class GeyserHandler {
    public static void register() {
        SoundReplacer.loadSoundMappings();
        CommandExecutor.loadCommands();
        registerUpstream();
        registerDownstream();
        SERVER.debug("Injected translators: InteractPacket -> " + Registries.BEDROCK_PACKET_TRANSLATORS.get(InteractPacket.class).getClass().getName()
                + ", PlayerAuthInputPacket -> " + Registries.BEDROCK_PACKET_TRANSLATORS.get(PlayerAuthInputPacket.class).getClass().getName());
    }

    public static void registerUpstream() {
        // Combat sounds
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundSoundPacket.class, new JavaSoundInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundSoundEntityPacket.class, new JavaSoundEntityInjector());
        // Shield stuff
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundEntityEventPacket.class, new JavaEntityEventInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundOpenScreenPacket.class, new JavaOpenScreenInjector());
        // Recipe menus (anvil and smithing table shown as a chest)
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundContainerSetContentPacket.class, new JavaContainerSetContentInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundContainerSetSlotPacket.class, new JavaContainerSetSlotInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundContainerSetDataPacket.class, new JavaContainerSetDataInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundContainerClosePacket.class, new JavaContainerCloseInjector());
        // Ticking state
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundTickingStatePacket.class, new JavaTickingStateInjector());
        // Auto reconnect (doesn't work, should look into)
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundDisconnectPacket.class, new JavaDisconnectInjector());

        // Tab list
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundTabListPacket.class, new JavaTabListInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundPlayerInfoUpdatePacket.class, new JavaPlayerInfoUpdateInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundPlayerInfoRemovePacket.class, new JavaPlayerInfoRemoveInjector());
        // Server Links (1.21+)
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundServerLinksPacket.class, new JavaServerLinksInjector());


        // Emote chat dimension workarounds
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundAddEntityPacket.class, new JavaAddEntityInjector());
        Registries.JAVA_PACKET_TRANSLATORS.register(ClientboundRemoveEntitiesPacket.class, new JavaRemoveEntityInjector());

    }

    public static void registerDownstream() {
        if (ServerType.isExtension()) {
            Registries.BEDROCK_PACKET_TRANSLATORS.register(CommandRequestPacket.class, new BedrockCommandRequestInjector());
        }
        // Cooldown
        Registries.BEDROCK_PACKET_TRANSLATORS.register(PlayerAuthInputPacket.class, new BedrockPlayerAuthInputInjector());
        Registries.BEDROCK_PACKET_TRANSLATORS.register(InventoryTransactionPacket.class, new BedrockInventoryTransactionInjector());

        // Settings
        Registries.BEDROCK_PACKET_TRANSLATORS.register(ServerSettingsRequestPacket.class, new BedrockServerSettingsRequestInjector());

        // FPS Counter
        // why isnt this registering even though its being sent AAAAAAAAAAAAAAAAAAAAAAAAAAA
        // update: 2.6.1 of geyser just magically fixed this!!
        Registries.BEDROCK_PACKET_TRANSLATORS.register(ServerboundDiagnosticsPacket.class, new BedrockDiagnosticsInjector());

        /* Action intercept related */
        // PICK_BLOCK
        Registries.BEDROCK_PACKET_TRANSLATORS.register(BlockPickRequestPacket.class, new BedrockBlockPickRequestInjector());
        Registries.BEDROCK_PACKET_TRANSLATORS.register(EntityPickRequestPacket.class, new BedrockEntityPickRequestInjector());
        // OPEN_INVENTORY and cooldown stuff
        Registries.BEDROCK_PACKET_TRANSLATORS.register(InteractPacket.class, new BedrockInteractInjector());
    }
}
