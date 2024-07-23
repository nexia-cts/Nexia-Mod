package com.nexia.core.mixin.item;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceBottleItem.class)
public class ExperienceBottleItemMixin {
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void preventPlayers(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        NexiaPlayer nexiaPlayer = new NexiaPlayer((ServerPlayer) player);

        if((player.level.equals(LobbyUtil.lobbyWorld) && !KitRoom.isInKitRoom(nexiaPlayer)) && !player.isCreative()) {
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(interactionHand)));
            nexiaPlayer.refreshInventory();
        }
    }
}
