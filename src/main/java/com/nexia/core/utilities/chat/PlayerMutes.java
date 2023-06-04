package com.nexia.core.utilities.chat;

import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.player.SavedPlayerData;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PlayerMutes {

    public static void mute(CommandSourceStack sender, ServerPlayer muted, int duration, String reason) {
        if (PlayerUtil.hasPermission(muted.createCommandSourceStack(), "nexia.staff.mute", 1)) {
            sender.sendSuccess(ChatFormat.format("{f}You can't mute staff."), false);
            return;
        }

        SavedPlayerData mutedData = PlayerDataManager.get(muted).savedData;
        long currentMuteTime = mutedData.muteEnd - System.currentTimeMillis();

        if (currentMuteTime > 0) {
            sender.sendSuccess(ChatFormat.format("{s}This player has already been muted for {f}{}{s}." +
                    "\n{s}Reason: {f}{}", muteTimeToText(currentMuteTime), mutedData.muteReason), false);
            return;
        }

        mutedData.muteEnd = System.currentTimeMillis() + duration;
        mutedData.muteReason = reason;

        sender.sendSuccess(ChatFormat.format("{s}Muted {b2}{} {s}for {b2}{}{s}." +
                "\n{s}Reason: {b2}{}", muted.getScoreboardName(), muteTimeToText(duration), reason), false);
        muted.sendMessage(ChatFormat.format("{s}You have been muted for {f}{}{s}." +
                "\n{s}Reason: {f}{}", muteTimeToText(duration), reason), Util.NIL_UUID);

    }

    public static void unMute(CommandSourceStack sender, ServerPlayer unMuted) {
        SavedPlayerData unMutedData = PlayerDataManager.get(unMuted).savedData;
        long currentMuteTime = unMutedData.muteEnd - System.currentTimeMillis();

        if (currentMuteTime <= 0) {
            sender.sendSuccess(ChatFormat.format("{s}This player is not muted."), false);
            return;
        }

        unMutedData.muteEnd = System.currentTimeMillis();
        unMutedData.muteReason = null;

        sender.sendSuccess(ChatFormat.format("{s}Unmuted {b2}{}{s}.", unMuted.getScoreboardName()), false);
        unMuted.sendMessage(ChatFormat.format("{s}You have been unmuted."), Util.NIL_UUID);
    }

    public static boolean muted(ServerPlayer player) {
        SavedPlayerData savedData = PlayerDataManager.get(player).savedData;
        long muteTime = savedData.muteEnd - System.currentTimeMillis();
        String reason = savedData.muteReason;

        if (muteTime > 0) {
            player.sendMessage(ChatFormat.format("{s}You have been muted for {f}{}{s}." +
                    "\n{s}Reason: {f}{}", muteTimeToText(muteTime), reason), Util.NIL_UUID);
            return true;
        }
        return false;
    }

    private static String muteTimeToText(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        long hours = minutes / 60;
        minutes %= 60;

        return hours + "h, " + minutes + "m, " + seconds + "s";
    }

}
