package com.nexia.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugStickItem.class)
public class DebugStickItemMixin {
    @ModifyExpressionValue(
            method = "handleInteraction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"
            )
    )
    public boolean addDebugStickUsePermission(boolean original, Player player, BlockState blockState) {
        return Permissions.check(player,"nexia.staff.debug_stick", original);
    }
}
