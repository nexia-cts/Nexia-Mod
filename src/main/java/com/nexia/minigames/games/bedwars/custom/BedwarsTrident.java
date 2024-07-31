package com.nexia.minigames.games.bedwars.custom;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.minigames.games.bedwars.BedwarsGame;
import com.nexia.minigames.games.bedwars.util.BedwarsUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class BedwarsTrident extends ThrownTrident {

    public static final int returnTime = 9 * 20;
    public static final String itemTagKey = "BedWarsTrident";

    public ItemStack itemStack;
    public ServerPlayer owner;
    public boolean willBeReturned;
    public int age;

    public BedwarsTrident(Level level, ServerPlayer player, ItemStack itemStack) {
        super(level, player, itemStack);
        this.itemStack = itemStack.copy();
        this.owner = player;
        this.willBeReturned = !player.abilities.instabuild;
        this.age = 0;

        NexiaPlayer nexiaPlayer = new NexiaPlayer(this.owner);
        if (BedwarsUtil.isBedWarsPlayer(nexiaPlayer)) {
            if (!BedwarsGame.gameTridents.containsKey(nexiaPlayer)) {
                BedwarsGame.gameTridents.put(nexiaPlayer, new ArrayList<>());
            }
            BedwarsGame.gameTridents.get(nexiaPlayer).add(this);
        }
    }

    public void tick() {
        super.tick();

        if (age >= returnTime) {
            this.remove();
            if (willBeReturned) {
                this.owner.inventory.add(this.itemStack);
                NexiaPlayer nexiaOwner = new NexiaPlayer(this.owner);
                nexiaOwner.sendSound(SoundEvents.TRIDENT_RETURN, SoundSource.NEUTRAL, 2f, 1f);
                nexiaOwner.sendSound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.05f, 2f);

            }
        }
        age++;
    }

    public void outOfWorld() {
        this.noPhysics = true;
    }
}
