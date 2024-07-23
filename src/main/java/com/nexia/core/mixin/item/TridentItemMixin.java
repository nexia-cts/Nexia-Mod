package com.nexia.core.mixin.item;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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

    @Redirect(method = "releaseUsing", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/ThrownTrident;"))
    private ThrownTrident setThrownTrident(Level level, LivingEntity livingEntity, ItemStack itemStack) {

        if (livingEntity instanceof ServerPlayer player) {

            NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
            if (BwUtil.isBedWarsPlayer(nexiaPlayer)) {
                return BwPlayerEvents.throwTrident(nexiaPlayer, itemStack);
            }

        }
        return new ThrownTrident(level, livingEntity, itemStack);
    }


    @Inject(method = "releaseUsing", at = @At(value = "HEAD"), cancellable = true)
    public void changeHoldTime(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if(livingEntity instanceof ServerPlayer player){
            NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

            if((FfaAreas.isFfaWorld(player.level) && FfaAreas.isInFfaSpawn(nexiaPlayer)) ||
                    (com.nexia.ffa.kits.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer)) ||
                            (com.nexia.ffa.uhc.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.uhc.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer)) ||
                                    (((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(PlayerGameMode.LOBBY) && ((DuelsPlayerData)PlayerDataManager.getDataManager(DuelGameHandler.DUELS_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(DuelGameMode.LOBBY))
            ) { ci.cancel(); }
        }
    }
}
