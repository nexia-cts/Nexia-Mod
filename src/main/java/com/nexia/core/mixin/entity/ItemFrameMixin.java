package com.nexia.core.mixin.entity;

import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.notcoded.codelib.players.AccuratePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin extends Entity {

    public ItemFrameMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interact", cancellable = true, at = @At("HEAD"))
    private void canTakeItem(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(serverPlayer));

        // Disable interacting with item frames
        if ((FfaUtil.isFfaPlayer(nexiaPlayer) || KitRoom.isInKitRoom(nexiaPlayer)) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

    }
}
