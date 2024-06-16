package com.nexia.ffa.kits.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KillTracker {
    private static final Map<UUID, Map<UUID, Integer>> killCounts = new HashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> encounterCounts = new HashMap<>();
    private static final Map<UUID, Set<UUID>> uniqueOpponents = new HashMap<>();
    private static final Map<UUID, Integer> totalFights = new HashMap<>();

    public static void incrementKillCount(UUID attacker, UUID victim) {
        // Increment kill count
        killCounts.computeIfAbsent(attacker, k -> new HashMap<>()).merge(victim, 1, Integer::sum);

        // Increment encounter count for both attacker and victim
        encounterCounts.computeIfAbsent(attacker, k -> new HashMap<>()).merge(victim, 1, Integer::sum);
        encounterCounts.computeIfAbsent(victim, k -> new HashMap<>()).merge(attacker, 1, Integer::sum);

        // Update unique opponents for both attacker and victim
        uniqueOpponents.computeIfAbsent(attacker, k -> new HashSet<>()).add(victim);
        uniqueOpponents.computeIfAbsent(victim, k -> new HashSet<>()).add(attacker);

        // Increment total fights for both attacker and victim
        totalFights.put(attacker, totalFights.getOrDefault(attacker, 0) + 1);
        totalFights.put(victim, totalFights.getOrDefault(victim, 0) + 1);
    }

    public static int getKillCount(UUID attacker, UUID victim) {
        return killCounts.getOrDefault(attacker, new HashMap<>()).getOrDefault(victim, 0);
    }

    public static int getTotalFights(UUID player) {
        return totalFights.getOrDefault(player, 0);
    }

    public static int getUniqueOpponentsCount(UUID player) {
        return uniqueOpponents.getOrDefault(player, new HashSet<>()).size();
    }
}
