package com.nexia.core.mixin.item;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.DuelGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public class EggItemMixin {

    @Unique
    private InteractionHand hand;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        NexiaPlayer nexiaPlayer = new NexiaPlayer((ServerPlayer) player);

        if((com.nexia.core.utilities.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(PlayerGameMode.LOBBY) && com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(DuelGameMode.LOBBY))) {
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(interactionHand)));
            ItemStackUtil.sendInventoryRefreshPacket((ServerPlayer) player);
        }
        this.hand = interactionHand;
    }

    @Redirect(method = "use", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/projectile/ThrownEgg;"))
    private ThrownEgg setThrownEgg(Level level, LivingEntity livingEntity) {

        if (livingEntity instanceof ServerPlayer player) {

            if (BwAreas.isBedWarsWorld(level)) {
                return BwPlayerEvents.throwEgg(new NexiaPlayer(player), player.getItemInHand(hand));
            }

        }
        return new ThrownEgg(level, livingEntity);
    }

}
