package com.nexia.core.mixin.item;

import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashPotionItem.class)
public class SplashPotionItemMixin {
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void preventFFAplayers(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if((FfaKitsUtil.isFfaPlayer(player) && FfaKitsUtil.wasInSpawn.contains(player.getUUID())) || (PlayerDataManager.get(player).gameMode.equals(DuelGameMode.LOBBY))) {
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(interactionHand)));
            ItemStackUtil.sendInventoryRefreshPacket((ServerPlayer) player);
        }
    }
}