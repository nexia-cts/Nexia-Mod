package com.nexia.core.mixin.item;

import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public class EggItemMixin {

    private InteractionHand hand;

    @Inject(method = "use", at = @At("HEAD"))
    private void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        this.hand = interactionHand;
    }

    @Redirect(method = "use", at = @At(value = "NEW", target = "net/minecraft/world/entity/projectile/ThrownEgg"))
    private ThrownEgg setThrownEgg(Level level, LivingEntity livingEntity) {

        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) livingEntity;

            if (BwAreas.isBedWarsWorld(level)) {
                return BwPlayerEvents.throwEgg(player, player.getItemInHand(hand));
            }

        }
        return new ThrownEgg(level, livingEntity);
    }

}
