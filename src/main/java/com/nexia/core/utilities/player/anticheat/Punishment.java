package com.nexia.core.utilities.player.anticheat;

import com.nexia.core.utilities.player.BanHandler;
import net.blumbo.blfscheduler.BlfRunnable;
import net.blumbo.blfscheduler.BlfScheduler;
import net.minecraft.server.level.ServerPlayer;

public class Punishment {

    private boolean repeat;

    public int reachOffences;

    public int noSwingOffences;

    public Punishment() {
        this.repeat = true;

        this.reachOffences = 0;
        this.noSwingOffences = 0;

        this.main();
    }

    private void main() {
        if(!repeat) return;

        // every 10 minutes it clears
        BlfScheduler.repeat(20, 12000, new BlfRunnable() {
            @Override
            public void run() {
                if(!repeat) return;
                clear();
            }
        });
    }

    private void clear() {
        this.reachOffences = 0;
        this.noSwingOffences = 0;
    }

    public void check(ServerPlayer player) {
        int limit = 4;

        if(this.reachOffences >= limit) {
            BanHandler.handlePunishment(player, Type.REACH);
            this.reachOffences = 0;
            return;
        }

        if(this.noSwingOffences >= limit) {
            BanHandler.handlePunishment(player, Type.NO_SWING);
            this.noSwingOffences = 0;
            return;
        }

    }

    public enum Type {
        REACH,
        NO_SWING
    }

    public String toString() {
        return String.format("Punishment[repeat=%s,reachOffences=%s,noSwingOffences=%s]", repeat, reachOffences, noSwingOffences);
    }
}
