package com.nexia.ffa.kits.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillTracker {
    private static final Map<UUID, Map<UUID, Integer>> killCounts = new HashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> encounterCounts = new HashMap<>();

    public static void incrementKillCount(UUID attacker, UUID victim) {
        killCounts.computeIfAbsent(attacker, k -> new HashMap<>()).merge(victim, 1, Integer::sum);
        encounterCounts.computeIfAbsent(attacker, k -> new HashMap<>()).merge(victim, 1, Integer::sum);
        encounterCounts.computeIfAbsent(victim, k -> new HashMap<>()).merge(attacker, 1, Integer::sum);
    }

    public static int getKillCount(UUID attacker, UUID victim) {
        return killCounts.getOrDefault(attacker, new HashMap<>()).getOrDefault(victim, 0);
    }

}
