package com.nexia.core.mixin.player;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
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

        if (BwAreas.isBedWarsWorld(serverPlayer.level) || FfaSkyUtil.isFfaPlayer(serverPlayer)) {
            return 0.5f;
        }

        if(FfaUhcUtil.isFfaPlayer(serverPlayer)) {
            return 0.0f;
        }

        return 1f;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void modifyHunger(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        FoodData data = (FoodData)(Object)this;

        if (BwUtil.isInBedWars(serverPlayer)) {
            BwPlayerEvents.afterHungerTick((FoodData)(Object)this);
            BwPlayerEvents.afterHungerTick(data);
        }

        if(SkywarsGame.isSkywarsPlayer(serverPlayer)) return;

        // Duels
        DuelGameMode duelGameMode = PlayerDataManager.get(player).gameMode;
        PlayerGameMode gameMode = com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode;
        if(gameMode.equals(PlayerGameMode.LOBBY) && !duelGameMode.hasSaturation) return;

        data.setFoodLevel(20);
    }

}
