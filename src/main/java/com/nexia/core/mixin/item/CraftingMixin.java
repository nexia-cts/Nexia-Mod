package com.nexia.core.mixin.item;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingMixin {
    @Unique
    private static ServerPlayer crafter;
    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
    private static void craft(int i, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, CallbackInfo ci) {
        if (player instanceof ServerPlayer) {
            crafter = (ServerPlayer) player;
        }
    }

    @ModifyArg(method = "slotChangedCraftingGrid", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private static ItemStack setCraftResult(ItemStack itemStack) {
        if (crafter == null) return itemStack;

        if (PlayerDataManager.get(crafter).gameMode == PlayerGameMode.LOBBY) {
            ItemStackUtil.sendInventoryRefreshPacket(crafter);
            return ItemStack.EMPTY;
        }

        return itemStack;
    }

    @ModifyArg(method = "slotChangedCraftingGrid", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundContainerSetSlotPacket;<init>(IILnet/minecraft/world/item/ItemStack;)V"))
    private static ItemStack setCraftResultPacketItem(ItemStack itemStack) {
        if (crafter == null) return itemStack;

        if (PlayerDataManager.get(crafter).gameMode == PlayerGameMode.LOBBY) return ItemStack.EMPTY;

        return itemStack;
    }

}