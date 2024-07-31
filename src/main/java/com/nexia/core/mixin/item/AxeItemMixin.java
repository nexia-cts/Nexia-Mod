package com.nexia.core.mixin.item;

import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.bedwars.players.BedwarsPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    @Inject(method = "useOn", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/UseOnContext;getPlayer()Lnet/minecraft/world/entity/player/Player;"))
    private void stripeWood(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player playerEntity = context.getPlayer();
        if (!(playerEntity instanceof ServerPlayer player)) return;

        if (BedwarsAreas.isBedWarsWorld(context.getLevel())) {
            if (!BedwarsPlayerEvents.beforeStripWood(player, context)) {
                cir.setReturnValue(InteractionResult.PASS);
            }
        }
    }

}
