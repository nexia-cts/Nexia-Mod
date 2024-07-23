package com.nexia.discord.utilities.player;

import com.nexia.base.player.SavedPlayerData;

public class DiscordSavedPlayerData extends SavedPlayerData {

    public long discordID;
    public boolean isLinked;

    public DiscordSavedPlayerData() {
        super();
        this.discordID = 0;
        this.isLinked = false;
        try {
            buildField(Long.class, "discordID", discordID, DiscordSavedPlayerData.class);
            buildField(Boolean.class, "isLinked", isLinked, DiscordSavedPlayerData.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
