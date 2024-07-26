package com.nexia.core.networking;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;

public class NetworkingHandler {
    public static ResourceLocation detectionNetworkChannel = new ResourceLocation("nexia", "networking");

    // Combatify >= 1.2.0 Beta 6
    public static ResourceLocation detectCombatify = new ResourceLocation("combatify", "remaining_use_ticks");

    // Combatify < 1.2.0 Beta 6
    public static ResourceLocation detectOldCombatify = new ResourceLocation("combatify", "atlas_config");

    public NetworkingHandler() {
        ServerPlayConnectionEvents.JOIN.register(detectionNetworkChannel,(handler, sender, server) -> {
            PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).addPlayerData(handler.player.getUUID());
            CorePlayerData playerData = ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(handler.player.getUUID()));

            if(!ServerPlayNetworking.canSend(handler.player, detectCombatify) && !ServerPlayNetworking.canSend(handler.player, detectOldCombatify)) {

                // Fabric API 0.42.0 (1.16.5) only has 'registry/sync' whilst newer versions have 'registry/sync/direct'
                // Downside is, the client needs to have Fabric API installed to be able to detect.

                if(ServerPlayNetworking.canSend(handler.player, new ResourceLocation("fabric", "registry/sync/direct"))) {
                    playerData.setClientType(CorePlayerData.ClientType.VIAFABRICPLUS);
                    handler.player.connection.disconnect(ObjectMappings.convertComponent(
                            ChatFormat.nexia
                                    .append(Component.text("\n"))
                                    .append(Component.text("You need to install", ChatFormat.systemColor))
                                    .append(Component.text(" Combatify ", ChatFormat.Minecraft.red).decoration(ChatFormat.bold, true))
                                    .append(Component.text("in order to join the server.", ChatFormat.systemColor))
                                    .append(Component.text("\n\n"))
                                    .append(Component.text("https://modrinth.com/mod/combatify", ChatFormat.Minecraft.red))
                    ));
                    return;
                }

                playerData.setClientType(CorePlayerData.ClientType.COMBAT_TEST);
                return;
            }

            playerData.setClientType(CorePlayerData.ClientType.COMBATIFY);
        });
    }
}
