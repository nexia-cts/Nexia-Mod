package com.nexia.core.mixin.item;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.pot.utilities.FfaPotUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {SnowballItem.class, SplashPotionItem.class, LingeringPotionItem.class, FishingRodItem.class, EggItem.class})
public class MultipleItemMixins {
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void preventPlayers(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        NexiaPlayer nexiaPlayer = new NexiaPlayer((ServerPlayer) player);

        if((FfaPotUtil.INSTANCE.isFfaPlayer(nexiaPlayer) && FfaPotUtil.INSTANCE.wasInSpawn.contains(player.getUUID())) || ((FfaKitsUtil.INSTANCE.isFfaPlayer(nexiaPlayer) && FfaKitsUtil.INSTANCE.wasInSpawn.contains(player.getUUID())) || player.level.equals(LobbyUtil.lobbyWorld)) && !player.isCreative()) {
            cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(interactionHand)));
            nexiaPlayer.refreshInventory();
        }
    }
}
