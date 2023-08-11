package com.nexia.core.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "nexia-core")
public class ModConfig implements ConfigData {

    @Comment("List of ranks.")
    public String[] ranks = {""};
    public double[] lobbyPos = {
            0.5,
            65.0,
            0.5,
    };


    public Enhancements enhancements = new Enhancements();
    public static class Enhancements {
        @Comment("Toggles if the better shields are enabled.")
        public boolean betterShields = false;

        @Comment("Modifies the pearl cooldown in ticks (20 ticks = 1 second).")
        public int enderpearlCooldown = 20;

        @Comment("Remove Projectiles going through players [EXPERIMENTAL]")
        public boolean projectilePatch = false;
    }
}
