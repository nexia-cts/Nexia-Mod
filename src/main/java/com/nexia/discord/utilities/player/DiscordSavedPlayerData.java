package com.nexia.discord.utilities.player;

import com.nexia.base.player.SavedPlayerData;

public class DiscordSavedPlayerData extends SavedPlayerData {

    public long discordID;
    public boolean isLinked;

    public DiscordSavedPlayerData() {
        super();
        this.discordID = 0;
        this.isLinked = false;
        set(Long.class, "discordID", discordID);
        set(Boolean.class, "isLinked", isLinked);
    }
}
