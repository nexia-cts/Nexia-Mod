package com.nexia.core.utilities.player;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.notcoded.codelib.players.AccuratePlayer;

import java.util.Objects;

public record NexiaPlayer(AccuratePlayer player) {

    public void reset(boolean heal) {
        this.player.get().setInvulnerable(false);
        this.player.get().removeAllEffects();
        this.player.get().inventory.clearContent();
        this.player.get().setExperienceLevels(0);
        this.player.get().setExperiencePoints(0);
        this.player.get().inventory.setCarried(ItemStack.EMPTY);
        this.player.get().getEnderChestInventory().clearContent();
        this.player.get().setGlowing(false);

        if (heal) {
            this.player.get().setHealth(this.player.get().getMaxHealth());
            this.player.get().getFoodData().setFoodLevel(20);
        }


        this.player.get().abilities.mayfly = false;
        this.player.get().abilities.flying = false;
        Objects.requireNonNull(this.player.get().getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0.0);
        this.player.get().onUpdateAbilities();

        ServerTime.minecraftServer.getPlayerList().sendPlayerPermissionLevel(this.player.get());
    }

    public void leaveAllGames() {
        ServerPlayer minecraftPlayer = this.player.get();

        if (BwUtil.isInBedWars(minecraftPlayer)) {
            BwPlayerEvents.leaveInBedWars(minecraftPlayer);
        } else if (FfaUtil.isFfaPlayer(minecraftPlayer)) {
            FfaUtil.leaveOrDie(minecraftPlayer, minecraftPlayer.getLastDamageSource(), true);
        } else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.LOBBY) {
            DuelGameHandler.leave(minecraftPlayer, false);
        } else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.OITC) {
            OitcGame.leave(minecraftPlayer);
        } else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.SKYWARS) {
            SkywarsGame.leave(minecraftPlayer);
        } else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.FOOTBALL) {
            FootballGame.leave(minecraftPlayer);
        }

        for (String tag : LobbyUtil.removedTags) {
            if (this.player.get().getTags().contains(tag)) this.player.get().removeTag(tag);
        }
    }
}
