package com.nexia.minigames.games.bedwars.upgrades;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;

public class BwApplyTraps {

    public static void trapSecond() {
        // Loop all bedwars players
        for (BwTeam attackerTeam : BwTeam.allTeams.values()) {
            for (NexiaPlayer attacker : attackerTeam.players) {
                if (BwGame.respawningList.containsKey(attacker)) continue;

                // Loop all bedwars bases
                for (EntityPos trapPos : BwTrap.trapLocations.keySet()) {
                    BwTeam defenderTeam = BwTrap.trapLocations.get(trapPos);
                    if (defenderTeam == attackerTeam || !trapPos.isInRadius(new EntityPos(attacker.unwrap()), 19)) continue;

                    // Loop all traps in a bedwars base
                    boolean trapActivated = false;
                    HashMap<String, BwTrap> teamTrapSet = BwTrap.trapLocations.get(trapPos).traps;
                    for (String trapKey : teamTrapSet.keySet()) {
                        BwTrap trap = teamTrapSet.get(trapKey);
                        if (trap == null || !trap.bought) continue;
                        trapSetOff(attacker.unwrap(), trapKey);
                        trapActivated = true;
                        trap.bought = false;
                    }
                    if (trapActivated) alarmDefenders(defenderTeam);
                    break;
                }
            }
        }
    }

    private static void trapSetOff( ServerPlayer attacker, String trapKey) {
        switch (trapKey) {
            case BwTrap.TRAP_KEY_ALARM -> alarmTrap(attacker);
            case BwTrap.TRAP_KEY_SLOWNESS -> slownessTrap(attacker);
            case BwTrap.TRAP_KEY_MINING_FATIGUE -> miningTrap(attacker);
        }
    }

    private static void alarmDefenders(BwTeam defenderTeam) {
        PlayerUtil.broadcastSound(defenderTeam.players, SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 0.8f, 1.0f);
        for(NexiaPlayer player : defenderTeam.players) {
            player.sendTitle(Title.title(Component.text("Trap triggered!", ChatFormat.failColor), Component.text("")));
        }
    }

    private static void alarmTrap(ServerPlayer attacker) {
        if (attacker.hasEffect(MobEffects.INVISIBILITY)) {
            attacker.removeEffect(MobEffects.INVISIBILITY);
        }
    }

    private static void slownessTrap(ServerPlayer attacker) {
        int seconds = 10;
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, seconds * 20));
        attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, seconds * 20));
    }

    private static void miningTrap(ServerPlayer attacker) {
        attacker.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10 * 20));
    }

}
