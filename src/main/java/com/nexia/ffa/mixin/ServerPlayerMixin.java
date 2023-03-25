package com.nexia.ffa.mixin;

import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.ffa.utilities.player.PlayerDataManager;
import com.nexia.ffa.utilities.player.SavedPlayerData;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    public float calculateHealth(float health){
        float fixedHealth = Float.parseFloat(new DecimalFormat("#.#").format(health / 2));

        if(fixedHealth < 0.5){
            return 0.5f;
        }
        if(fixedHealth >= 10){
            return 10f;
        }

        if(fixedHealth % 1 == 0){ return fixedHealth; }

        if(Float.parseFloat(new DecimalFormat("0.#").format(fixedHealth)) >= 0.5){
            return Float.parseFloat(new DecimalFormat("#.5").format(fixedHealth));
        }
        return Float.parseFloat(new DecimalFormat("#.0").format(fixedHealth));
    }

    public void calculateDeath(ServerPlayer player){
        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }

        if(data.killstreak >= 5) {
            for (int i = 0; i < Main.server.getPlayerCount(); i++) {
                if (FfaUtil.isFfaPlayer(PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]))) {
                    PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(ChatFormat.format("§8[§c☠§8] §c{n}{} {s}has lost their killstreak of §c{b}{}{s}.", player.getScoreboardName(), data.killstreak), Util.NIL_UUID);
                }
            }
        }
        data.killstreak = 0;
    }

    public void calculateKill(ServerPlayer player){
        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.killstreak++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }
        data.kills++;
        player.heal(player.getMaxHealth());

        if(data.killstreak % 5 == 0){
            for(int i = 0; i < Main.server.getPlayerCount(); i++){
                if(FfaUtil.isFfaPlayer(PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]))){
                    PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(ChatFormat.format("§8[§c☠§8] §a{n}{} {s}now has a killstreak of §c{b}{}{s}!", player.getScoreboardName(), data.killstreak), Util.NIL_UUID);
                }
            }
        }
    }


    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo info) {
        ServerPlayer victim = (ServerPlayer) (Object) this;
        boolean attackerNull = !(source.getEntity() instanceof ServerPlayer);
        boolean victimTag = FfaUtil.isFfaPlayer(victim);

        if((attackerNull && victimTag) || (!attackerNull && source.getEntity() == victim && victimTag)){
            for(int i = 0; i < Main.server.getPlayerCount(); i++){
                if(FfaUtil.isFfaPlayer(PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]))){
                    PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(new TextComponent("§7Wow, §c☠ " + victim.getScoreboardName() + " §7somehow killed themselves."), Util.NIL_UUID);
                }
            }
            return;
        }
        if(victimTag){
            calculateDeath(victim);
        }
        if(attackerNull) { return; }
        ServerPlayer attacker = (ServerPlayer) source.getEntity();
        boolean attackerTag = FfaUtil.isFfaPlayer(attacker);

        if(attackerTag && victimTag){
            Item handItem = attacker.getMainHandItem().getItem();
            if(handItem == Items.NETHERITE_SWORD){
                for(int i = 0; i < Main.server.getPlayerCount(); i++){
                    if(FfaUtil.isFfaPlayer(PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]))){
                        PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(new TextComponent("§c☠ " + victim.getScoreboardName() + " §7was killed by §a\uD83D\uDDE1 " +  source.getEntity().getScoreboardName() + " §7with §c" + calculateHealth(attacker.getHealth()) + "❤ §7left."), Util.NIL_UUID);
                    }
                }
            } else if(handItem == Items.TRIDENT){
                for(int i = 0; i < Main.server.getPlayerCount(); i++){
                    if(FfaUtil.isFfaPlayer(PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]))){
                        PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(new TextComponent("§c☠ " + victim.getScoreboardName() + " §7was killed by §a\uD83D\uDD31 " +  source.getEntity().getScoreboardName() + " §7with §c" + calculateHealth(attacker.getHealth()) + "❤ §7left."), Util.NIL_UUID);
                    }
                }
            } else if(handItem == Items.NETHERITE_AXE){
                for(int i = 0; i < Main.server.getPlayerCount(); i++){
                    if(FfaUtil.isFfaPlayer(PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]))){
                        PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(new TextComponent("§c☠ " + victim.getScoreboardName() + " §7was killed by §a\uD83E\uDE93 " +  source.getEntity().getScoreboardName() + " §7with §c" + calculateHealth(attacker.getHealth()) + "❤ §7left."), Util.NIL_UUID);
                    }
                }
            } else {
                for(int i = 0; i < Main.server.getPlayerCount(); i++){
                    if(FfaUtil.isFfaPlayer(PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]))){
                        PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(new TextComponent("§c☠ " + victim.getScoreboardName() + " §7was killed by §a◆ " +  source.getEntity().getScoreboardName() + " §7with §c" + calculateHealth(attacker.getHealth()) + "❤ §7left."), Util.NIL_UUID);
                    }
                }
            }
        }

        if(attackerTag){
            calculateKill(attacker);
        }

        /*
        SavedPlayerData victimData = PlayerDataManager.get(victim).savedData;
        SavedPlayerData attackerData = PlayerDataManager.get(attacker).savedData;

        victimData.lastHitEntity = null;
        attackerData.lastHitEntity = null;

         */


        //info.cancel();
    }
}
