package com.nexia.core.utilities.player;

public class SavedPlayerData {

    public long muteEnd;

    public String muteReason;

    public boolean isReportBanned;

    public SavedPlayerData() {
        this.muteEnd = System.currentTimeMillis();
        this.muteReason = null;

        this.isReportBanned = false;
    }

}
