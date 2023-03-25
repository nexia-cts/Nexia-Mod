package com.nexia.core.utilities.player;

public class SavedPlayerData {

    public long muteEnd;
    public String muteReason;

    public SavedPlayerData() {
        this.muteEnd = System.currentTimeMillis();
        this.muteReason = null;
    }

}
