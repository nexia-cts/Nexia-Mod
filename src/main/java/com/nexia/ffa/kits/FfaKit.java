package com.nexia.ffa.kits;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.kits.utilities.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class FfaKit {
    public static ArrayList<FfaKit> ffaKits = new ArrayList<>();
    public static ArrayList<String> stringFfaKits = new ArrayList<>();

    public String id;

    public ItemStack item;

    public static final FfaKit KNIGHT = new FfaKit("knight", new ItemStack(Items.DIAMOND_SWORD));
    public static final FfaKit POSEIDON = new FfaKit("poseidon", new ItemStack(Items.TRIDENT));
    public static final FfaKit BRUTE = new FfaKit("brute", new ItemStack(Items.NETHERITE_AXE));
    public static final FfaKit HUNTER = new FfaKit("hunter", new ItemStack(Items.CROSSBOW));
    public static final FfaKit NINJA = new FfaKit("ninja", new ItemStack(Items.SUGAR));
    public static final FfaKit REAPER = new FfaKit("reaper", new ItemStack(Items.NETHERITE_HOE));

    public FfaKit(String id, ItemStack item) {
        this.id = id;
        this.item = item;

        FfaKit.ffaKits.add(this);
        FfaKit.stringFfaKits.add(id);
    }

    public static FfaKit identifyKit(String name) {
        for(FfaKit kit : FfaKit.ffaKits) {
            if(kit.id.equalsIgnoreCase(name)) return kit;
        }
        return null;
    }
    public void giveKit(ServerPlayer player, boolean clearEffect) {
        Player fPlayer = PlayerUtil.getFactoryPlayer(player);
        PlayerDataManager.get(player).kit = this;

        if(clearEffect) fPlayer.clearEffects();
        InventoryUtil.loadInventory(player, "ffa_kits", this.id);
    }
}
