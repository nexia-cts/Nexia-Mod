package com.nexia.core.utilities.chat;

import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PlayerMutes {

    public static void mute(CommandSourceStack sender, ServerPlayer muted, int duration, String reason) {
        if (PlayerUtil.hasPermission(muted.createCommandSourceStack(), "nexia.staff.mute", 1)) {
            sender.sendSuccess(LegacyChatFormat.format("{f}You can't mute staff."), false);
            return;
        }

        SavedPlayerData mutedData = PlayerDataManager.get(muted).savedData;
        long currentMuteTime = mutedData.muteEnd - System.currentTimeMillis();

        if (currentMuteTime > 0) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player has already been muted for {f}{}{s}." +
                    "\n{s}Reason: {f}{}", muteTimeToText(currentMuteTime), mutedData.muteReason), false);
            return;
        }

        mutedData.muteEnd = System.currentTimeMillis() + duration;
        mutedData.muteReason = reason;

        sender.sendSuccess(LegacyChatFormat.format("{s}Muted {b2}{} {s}for {b2}{}{s}." +
                "\n{s}Reason: {b2}{}", muted.getScoreboardName(), muteTimeToText(duration), reason), false);

        PlayerUtil.getFactoryPlayer(muted).sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been muted for "))
                                        .append(Component.text(muteTimeToText(duration)).color(ChatFormat.brandColor2))
                                                .append(Component.text(".\nReason: "))
                                                        .append(Component.text(reason).color(ChatFormat.brandColor2))
        );

    }

    public static void unMute(CommandSourceStack sender, ServerPlayer unMuted) {
        SavedPlayerData unMutedData = PlayerDataManager.get(unMuted).savedData;
        long currentMuteTime = unMutedData.muteEnd - System.currentTimeMillis();

        if (currentMuteTime <= 0) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player is not muted."), false);
            return;
        }

        unMutedData.muteEnd = System.currentTimeMillis();
        unMutedData.muteReason = null;

        sender.sendSuccess(LegacyChatFormat.format("{s}Unmuted {b2}{}{s}.", unMuted.getScoreboardName()), false);

        PlayerUtil.getFactoryPlayer(unMuted).sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been unmuted."))
        );
    }

    public static boolean muted(ServerPlayer player) {
        SavedPlayerData savedData = PlayerDataManager.get(player).savedData;
        long muteTime = savedData.muteEnd - System.currentTimeMillis();
        String reason = savedData.muteReason;

        if (muteTime > 0) {

            PlayerUtil.getFactoryPlayer(player).sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("You have been muted for "))
                            .append(Component.text(muteTimeToText(muteTime)).color(ChatFormat.brandColor2))
                            .append(Component.text(".\nReason: "))
                            .append(Component.text(reason).color(ChatFormat.brandColor2))
            );
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
