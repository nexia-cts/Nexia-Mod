package com.nexia.core.mixin.item;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LingeringPotionItem.class)
public class LingeringPotionItemMixin {
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void preventKitFFAplayers(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        NexiaPlayer nexiaPlayer = new NexiaPlayer((ServerPlayer) player);

        if((com.nexia.ffa.kits.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer)) || (com.nexia.core.utilities.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(PlayerGameMode.LOBBY) && com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(DuelGameMode.LOBBY))) {
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(interactionHand)));
            ItemStackUtil.sendInventoryRefreshPacket((ServerPlayer) player);
        }
    }
}
