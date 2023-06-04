package com.nexia.minigames.games.bedwars.util;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import com.nexia.minigames.games.bedwars.upgrades.BwUpgrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

public class BwGen {

    public Item resource;
    public long delay;
    public long cap;
    public EntityPos location;
    public ArmorStand timerDisplay;
    public ArmorStand itemDisplay;

    private BwGen(Item resource, long delay, long cap, EntityPos location) {
        this.resource = resource;
        this.delay = delay;
        this.cap = cap;
        this.location = location;
        summonTimerDisplay();
        summonItemDisplay();
    }

    // Generator list ---------------------------------------------------------------------

    public static HashMap<Item, ArrayList<BwGen>> allGens = resetGens();

    public static HashMap<Item, ArrayList<BwGen>> resetGens() {
        HashMap<Item, ArrayList<BwGen>> gens = new HashMap<>();

        gens.put(Items.IRON_INGOT, new ArrayList<>());
        gens.put(Items.GOLD_INGOT, new ArrayList<>());
        gens.put(Items.DIAMOND, new ArrayList<>());
        gens.put(Items.EMERALD, new ArrayList<>());

        allGens = gens;
        return gens;
    }

    // Gen actions ------------------------------------------------------------------

    // Item generator tag format: "{genTagStart}_{item type}_{delay in ticks}_{max amount}", example: gen_diamond_20_48
    // Team generator tag format: "{genTagStart}_{team color}", example: gen_red
    public static final String genTagStart = "gen_";
    public static final String itemCapTagStart = "itemCap_";
    public static final String genTimerDisplayTag = "bedWarsGenTimer";
    public static final String genItemDisplayTag = "bedWarsGenItemDisplay";
    private static long ticks = 0;

    public static final int[] upgradeCosts = {2, 4, 6, 8};

    public static final int[] ironDelays = {30, 20, 15, 15, 12};
    public static final int[] goldDelays = {120, 80, 60, 60, 48};
    public static final int[] emeraldDelays = {0, 0, 0, 1200, 800};

    public static void genTick() {
        ticks++;
        if (BwGame.isGameActive) {
            itemGenTick();
            teamGenTick();
        }
    }

    private static void itemGenTick() {
        for (ArrayList<BwGen> list : allGens.values()) {
            if (list == null) continue;

            for (BwGen gen : list) {
                if (gen == null) continue;
                gen.rotateItemDisplay();

                // Spawn in the item
                long nextItemIn = gen.delay - (ticks % gen.delay);
                if (nextItemIn == gen.delay) {
                    summonGenItem(gen.location, new ItemStack(gen.resource), gen.cap);
                }
                // Change timer number
                if (gen.timerDisplay != null && nextItemIn % 20 == 0) {
                    gen.timerDisplay.setCustomName(new TextComponent(
                            ChatFormat.brandColor1 + "Spawns in " +
                            ChatFormat.brandColor2 + nextItemIn / 20 + ChatFormat.brandColor1 + " seconds"));
                }
            }
        }
    }

    private static void teamGenTick() {
        if (BwTeam.allTeams == null) return;

        for (BwTeam team : BwTeam.allTeams.values()) {
            if (team == null || team.genLocation == null) continue;

            int level;
            if (!team.upgrades.containsKey(BwUpgrade.UPGRADE_KEY_GENERATOR)) level = 1;
            else level = team.upgrades.get(BwUpgrade.UPGRADE_KEY_GENERATOR).level;

            if (ironDelays.length > level && ironDelays[level] > 0 && ticks % ironDelays[level] == 0) {
                summonGenItem(team.genLocation, new ItemStack(Items.IRON_INGOT), 48);
            }
            if (goldDelays.length > level && goldDelays[level] > 0 && ticks % goldDelays[level] == 0) {
                summonGenItem(team.genLocation, new ItemStack(Items.GOLD_INGOT), 8);
            }
            if (emeraldDelays.length > level && emeraldDelays[level] > 0 && ticks % emeraldDelays[level] == 0) {
                summonGenItem(team.genLocation, new ItemStack(Items.EMERALD), 2);
            }
        }
    }

    private void rotateItemDisplay() {
        double amount = 1.5 + 5 * (1 + Math.sin(Math.toRadians(3 * ticks)));
        itemDisplay.yRot += (float)amount;
    }

    private static void summonGenItem(EntityPos pos, ItemStack itemStack, long cap) {
        ItemEntity entity = new ItemEntity(BwAreas.bedWarsWorld, pos.x, pos.y, pos.z, itemStack);
        entity.setDeltaMovement(0, 0, 0);
        entity.addTag(itemCapTagStart + cap);
        BwAreas.bedWarsWorld.addFreshEntity(entity);
    }

    public static void createGens() {
        ticks = 1;
        AABB aabb = new AABB(BwAreas.bedWarsCorner1, BwAreas.bedWarsCorner2);
        Predicate<Entity> predicate = o -> true;

        for (Entity entity : BwAreas.bedWarsWorld.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            for (String tag : entity.getTags()) {
                if (tag.startsWith(genTagStart)) {
                    addPossibleGen(entity, tag);
                }
            }
        }
    }

    private static void addPossibleGen(Entity entity, String tag) {
        tag = tag.replaceFirst(genTagStart, "");

        if (BwTeam.allTeams.containsKey(tag)) {
            BwTeam.allTeams.get(tag).genLocation = new EntityPos(entity);
            return;
        }

        ArrayList<String> splitTag = new ArrayList<>(Arrays.asList(tag.split("_")));
        if (splitTag.size() < 3) return;

        long cap = getLastNumber(splitTag);
        if (cap < 0) return;
        long delay = getLastNumber(splitTag);
        if (delay < 0) return;
        Item item = ItemStackUtil.itemFromString(StringUtils.join(splitTag, "_"));
        if (!allGens.containsKey(item)) return;

        BwGen gen = new BwGen(item, delay, cap, new EntityPos(entity));
        allGens.get(item).add(gen);
    }

    private void summonTimerDisplay() {
        EntityPos pos = this.location;
        ArmorStand armorStand = new ArmorStand(BwAreas.bedWarsWorld, pos.x, pos.y + 2.4, pos.z);
        armorStand.setCustomNameVisible(true);
        armorStand.addTag(genTimerDisplayTag);
        makeInvisible(armorStand);

        BwAreas.bedWarsWorld.addFreshEntity(armorStand);
        timerDisplay = armorStand;
    }

    private void summonItemDisplay() {
        EntityPos pos = this.location;
        ArmorStand armorStand = new ArmorStand(BwAreas.bedWarsWorld, pos.x, pos.y + 1.8, pos.z);
        armorStand.addTag(genItemDisplayTag);
        makeInvisible(armorStand);

        Item item = Items.AIR;
        if (resource == Items.DIAMOND) item = Items.DIAMOND_BLOCK;
        else if (resource == Items.EMERALD) item = Items.EMERALD_BLOCK;
        armorStand.setItemSlot(EquipmentSlot.HEAD, item.getDefaultInstance());

        BwAreas.bedWarsWorld.addFreshEntity(armorStand);
        itemDisplay = armorStand;
    }

    private static void makeInvisible(ArmorStand armorStand) {
        armorStand.setNoGravity(true);
        armorStand.setInvisible(true);

        CompoundTag compoundTag = new CompoundTag();
        armorStand.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Marker", true);
        armorStand.readAdditionalSaveData(compoundTag);
    }

    private static long getLastNumber(ArrayList<String> splitTag) {
        String stringCap = splitTag.get(splitTag.size() - 1);
        if (!NumberUtils.isParsable(stringCap)) return -1;
        long number = Math.round(NumberUtils.toDouble(stringCap));
        splitTag.remove(splitTag.size() - 1);
        return number;
    }

}
