package com.nexia.core.mixin.item;


import com.nexia.ffa.utilities.FfaAreas;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "HEAD"), cancellable = true)
    public void changeHoldTime(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if(livingEntity instanceof Player player){
            if(FfaAreas.isFfaWorld(player.level) && FfaAreas.isInFfaSpawn(player)) { ci.cancel(); }
        }
    }
}
