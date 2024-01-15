package com.nexia.core.utilities.item;

import com.natamus.collective_fabric.functions.PlayerFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public class InventoryUtil {

    static String dirpath = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/inventories";

    public static void sendHandItemPacket(ServerPlayer player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            sendInvSlotPacket(player, player.inventory.selected);
        } else if (hand == InteractionHand.OFF_HAND) {
            sendInvSlotPacket(player, 40);
        }
    }

    public static void sendInvSlotPacket(ServerPlayer player, int slot) {
        int packetSlot;

        if (slot < 9) {
            packetSlot = 36 + slot;
        } else if (slot < 36) {
            packetSlot = slot;
        } else if (slot < 40) {
            packetSlot = 44 - slot;
        } else if (slot == 40) {
            packetSlot = 45;
        } else {
            return;
        }

        player.connection.send(new ClientboundContainerSetSlotPacket(0, packetSlot, player.inventory.getItem(slot)));
    }

    public static void loadInventory(@NotNull ServerPlayer player, @NotNull String inventoryName) {
        if(inventoryName.trim().isEmpty()) return;

        PlayerFunctions.setPlayerGearFromString(player, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(inventoryName));
    }

}
