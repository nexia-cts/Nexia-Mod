package com.nexia.core.mixin.item;

import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "HEAD"), cancellable = true)
    public void changeHoldTime(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if(livingEntity instanceof Player player){
            if(FfaAreas.isFfaWorld(player.level) && FfaAreas.isInFfaSpawn(player)) { ci.cancel(); }
        }
    }

    @Redirect(method = "releaseUsing", at = @At(value = "NEW", target = "net/minecraft/world/entity/projectile/ThrownTrident"))
    private ThrownTrident setThrownTrident(Level level, LivingEntity livingEntity, ItemStack itemStack) {

        if (livingEntity instanceof ServerPlayer player) {

            if (BwUtil.isBedWarsPlayer(player)) {
                return BwPlayerEvents.throwTrident(player, itemStack);
            }

        }
        return new ThrownTrident(level, livingEntity, itemStack);
    }
}
