package com.nexia.core.mixin.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingMixin {

    @Shadow @Final private Player player;
    // Set crafter to player
    private static ServerPlayer crafter;
    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
    private static void craft(int i, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, CallbackInfo ci) {
        if (player instanceof ServerPlayer) {
            crafter = (ServerPlayer) player;
        }
    }

}
