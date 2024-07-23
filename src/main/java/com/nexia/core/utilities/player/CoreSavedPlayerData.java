package com.nexia.core.utilities.player;

import com.nexia.base.player.SavedPlayerData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CoreSavedPlayerData extends SavedPlayerData {

    public String muteEnd;

    public String muteReason;

    public boolean isReportBanned;

    public boolean sprintFix;

    public CoreSavedPlayerData() {
        super();
        setMuteEnd(LocalDateTime.MIN);
        this.muteReason = null;
        this.isReportBanned = false;
        this.sprintFix = true;
        set(String.class, "muteEnd", muteEnd);
        set(String.class, "muteReason", muteReason);
        set(Boolean.class, "isReportBanned", isReportBanned);
        set(Boolean.class, "sprintFix", sprintFix);
    }

    public LocalDateTime getMuteEnd() {
        LocalDateTime localDateTime;
        try {
            localDateTime = LocalDateTime.parse(get(String.class, "muteEnd"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            localDateTime = LocalDateTime.MIN;
        }
        return localDateTime;
    }

    public void setMuteEnd(LocalDateTime muteEnd) {
        this.muteEnd = muteEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        set(String.class, "muteEnd", this.muteEnd);
    }

    public String getMuteReason() {
        return get(String.class, "muteReason");
    }

    public String setMuteReason(String muteReason) {
        set(String.class, "muteReason", muteReason);
        return this.muteReason;
    }

    public boolean isReportBanned() {
        return get(Boolean.class, "isReportBanned");
    }

    public boolean setReportBanned(boolean reportBanned) {
        set(Boolean.class, "isReportBanned", reportBanned);
        return isReportBanned;
    }

    public boolean isSprintFix() {
        return get(Boolean.class, "sprintFix");
    }

    public boolean setSprintFix(boolean sprintFix) {
        set(Boolean.class, "sprintFix", sprintFix);
        return this.sprintFix;
    }
}
