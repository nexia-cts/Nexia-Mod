package com.nexia.core.mixin.item;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.minigames.games.duels.DuelGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "HEAD"), cancellable = true)
    public void preventKitFFAplayers(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if(livingEntity instanceof Player player){
            NexiaPlayer nexiaPlayer = new NexiaPlayer((ServerPlayer) player);

            if((com.nexia.ffa.kits.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer)) ||
                    (com.nexia.ffa.uhc.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.uhc.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer)) ||
                    (com.nexia.ffa.sky.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.sky.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer)) ||
                    (com.nexia.core.utilities.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(PlayerGameMode.LOBBY) && com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(DuelGameMode.LOBBY))
            ) {
                ci.cancel();
                nexiaPlayer.refreshInventory();
            }
        }
    }
}
