package com.nexia.minigames.games.bedwars.custom;

import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class BwTrident extends ThrownTrident {

    public static final int returnTime = 9 * 20;
    public static final String itemTagKey = "BedWarsTrident";

    public ItemStack itemStack;
    public ServerPlayer owner;
    public boolean willBeReturned;
    public int age;

    public BwTrident(Level level, ServerPlayer player, ItemStack itemStack) {
        super(level, player, itemStack);
        this.itemStack = itemStack.copy();
        this.owner = player;
        this.willBeReturned = !player.abilities.instabuild;
        this.age = 0;

        NexiaPlayer nexiaPlayer = new NexiaPlayer(this.owner);
        if (BwUtil.isBedWarsPlayer(nexiaPlayer)) {
            if (!BwGame.gameTridents.containsKey(nexiaPlayer)) {
                BwGame.gameTridents.put(nexiaPlayer, new ArrayList<>());
            }
            BwGame.gameTridents.get(this.owner).add(this);
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
