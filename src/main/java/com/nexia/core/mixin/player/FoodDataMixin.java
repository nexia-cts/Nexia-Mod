package com.nexia.core.mixin.player;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Shadow private int foodLevel;
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

        if (BwAreas.isBedWarsWorld(serverPlayer.level)) {
            return 0.5f;
        }

        if(OitcGame.isOITCPlayer(serverPlayer)) {
            return 0.0f;
        }

        return 1f;
    }

}
