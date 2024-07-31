package com.nexia.minigames.games.bedwars.upgrades;

import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.bedwars.players.BedwarsTeam;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;

public class BedwarsTrap {

    // Nbt key for hashmap keys
    public static final String TRAP_TAG_KEY = "trapType";

    // Hashmap keys
    public static final String TRAP_KEY_SLOWNESS = "slowness";
    public static final String TRAP_KEY_MINING_FATIGUE = "mining_fatigue";
    public static final String TRAP_KEY_ALARM = "alarm";

    public static HashMap<EntityPos, BedwarsTeam> trapLocations = new HashMap<>();

    public static void resetTrapLocations() {
        trapLocations.clear();
        if (BedwarsTeam.allTeams == null) return;
        for (BedwarsTeam team : BedwarsTeam.allTeams.values()) {
            if (team.genLocation == null || team.bedLocation == null) continue;
            EntityPos trapLocation = team.genLocation.between(new EntityPos(team.bedLocation));
            trapLocation.y += 6;
            trapLocations.put(trapLocation, team);
        }
    }

    public ItemStack displayItem;
    public int displayRow;
    public int displayColumn;
    public boolean bought;

    private BedwarsTrap(int displayRow, int displayColumn, ItemStack displayItem) {
        this.displayItem = displayItem;
        this.displayRow = displayRow;
        this.displayColumn = displayColumn;
        this.bought = false;
    }

    public static HashMap<String, BedwarsTrap> newTrapSet() {
        HashMap<String, BedwarsTrap> newSet = new HashMap<>();

        newSet.put(TRAP_KEY_SLOWNESS, new BedwarsTrap(2, 2,
                BedwarsUpgrade.upgradeItemStack(Items.TRIPWIRE_HOOK, "Slowness & Blindness Trap", "Inflicts intruders with slowness\nand blindness for 10 seconds.")));
        newSet.put(TRAP_KEY_MINING_FATIGUE, new BedwarsTrap(2, 3,
                BedwarsUpgrade.upgradeItemStack(Items.STONE_PICKAXE, "Mining Fatigue Trap",
                        "Inflicts intruders with mining\nfatigue for 10 seconds.")));
        newSet.put(TRAP_KEY_ALARM, new BedwarsTrap(2, 4,
                BedwarsUpgrade.upgradeItemStack(Items.REDSTONE_TORCH, "Alarm Trap",
                        "Reveals invisible enemies\nentering your base.")));

        for (String key : newSet.keySet()) {
            newSet.get(key).displayItem.getOrCreateTag().putString(TRAP_TAG_KEY, key);
        }
        return newSet;
    }

}
