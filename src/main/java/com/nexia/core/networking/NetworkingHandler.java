package com.nexia.core.networking;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.Main;
import com.nexia.core.utilities.player.CorePlayerData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class NetworkingHandler {
    public static ResourceLocation detectionNetworkChannel = new ResourceLocation("nexia", "networking");

    public static ResourceLocation detectCombatify = new ResourceLocation("combatify", "atlas_config");

    public NetworkingHandler() {
        ServerPlayConnectionEvents.JOIN.register(detectionNetworkChannel,(handler, sender, server) -> {
            PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).addPlayerData(handler.player.getUUID());

            if(!ServerPlayNetworking.canSend(handler.player, detectCombatify)) {
                if(ServerPlayNetworking.canSend(handler.player, new ResourceLocation("fabric", "registry/sync/direct"))) {
                    ((CorePlayerData)PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).get(handler.player.getUUID())).clientType = CorePlayerData.ClientType.VIAFABRICPLUS;
                    handler.player.connection.disconnect(new TextComponent("§5§lNexia\n§7You need to install §c§lCombatify§7 in order to join the server.\n\n§chttps://modrinth.com/mod/combatify"));
                    return;
                }

                ((CorePlayerData)PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).get(handler.player.getUUID())).clientType = CorePlayerData.ClientType.COMBAT_TEST;
                return;
            }

            ((CorePlayerData)PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).get(handler.player.getUUID())).clientType = CorePlayerData.ClientType.COMBATIFY;
        });
    }
}
