package com.nexia.core.mixin.player;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Unique
    Player player;
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(Player player, CallbackInfo ci) {
        this.player = player;
    }

    // Nerf healing while in combat
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    private float heal(float par1) {
        if (!(player instanceof ServerPlayer serverPlayer)) return 1f;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);

        if (BwAreas.isBedWarsWorld(serverPlayer.level) || FfaSkyUtil.INSTANCE.isFfaPlayer(nexiaPlayer)) {
            return 0.5f;
        }

        if(FfaUhcUtil.INSTANCE.isFfaPlayer(nexiaPlayer)) {
            return 0.0f;
        }

        return 1f;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void modifyHunger(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);

        FoodData data = (FoodData)(Object)this;

        if (BwUtil.isInBedWars(nexiaPlayer)) {
            BwPlayerEvents.afterHungerTick((FoodData)(Object)this);
            BwPlayerEvents.afterHungerTick(data);
        }

        if(SkywarsGame.isSkywarsPlayer(nexiaPlayer)) return;

        // Duels
        DuelGameMode duelGameMode = ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaPlayer)).gameMode;
        PlayerGameMode gameMode = ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode;
        if(gameMode.equals(PlayerGameMode.LOBBY) && (duelGameMode != null && !duelGameMode.hasSaturation)) return;

        data.setFoodLevel(20);
    }

}
