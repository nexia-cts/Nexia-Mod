package com.nexia.core.utilities.chat;

import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.player.SavedPlayerData;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalDateTime;

public class PlayerMutes {

    public static void mute(CommandSourceStack sender, ServerPlayer muted, long duration, String reason) {
        if (Permissions.check(muted, "nexia.staff.mute", 1)) {
            sender.sendSuccess(LegacyChatFormat.format("{f}You can't mute staff."), false);
            return;
        }

        SavedPlayerData mutedData = PlayerDataManager.get(muted).savedData;
        LocalDateTime currentMuteTime = mutedData.getMuteEnd();

        if (LocalDateTime.now().isBefore(currentMuteTime)) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player has already been muted for {f}{}{s}." +
                    "\n{s}Reason: {f}{}", muteTimeToText(currentMuteTime), mutedData.getMuteReason()), false);
            return;
        }

        mutedData.setMuteEnd(LocalDateTime.now().plusSeconds(duration));
        mutedData.setMuteReason(reason);

        sender.sendSuccess(LegacyChatFormat.format("{s}Muted {b2}{} {s}for {b2}{}{s}." +
                "\n{s}Reason: {b2}{}", muted.getScoreboardName(), muteTimeToText(mutedData.getMuteEnd()), reason), false);

        PlayerUtil.getNexusPlayer(muted).sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been muted for ").decoration(ChatFormat.bold, false))
                                        .append(Component.text(muteTimeToText(mutedData.getMuteEnd())).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                .append(Component.text(".\nReason: ").decoration(ChatFormat.bold, false))
                                                        .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
        );

    }

    public static void unMute(CommandSourceStack sender, ServerPlayer unMuted) {
        SavedPlayerData unMutedData = PlayerDataManager.get(unMuted).savedData;
        LocalDateTime currentMuteTime = unMutedData.getMuteEnd();

        if (LocalDateTime.now().isAfter(currentMuteTime)) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player is not muted."), false);
            return;
        }

        unMutedData.setMuteEnd(LocalDateTime.MIN);
        unMutedData.setMuteReason(null);

        sender.sendSuccess(LegacyChatFormat.format("{s}Unmuted {b2}{}{s}.", unMuted.getScoreboardName()), false);

        PlayerUtil.getNexusPlayer(unMuted).sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been unmuted.").decoration(ChatFormat.bold, false))
        );
    }

    public static boolean muted(ServerPlayer player) {
        SavedPlayerData savedData = PlayerDataManager.get(player).savedData;
        LocalDateTime muteTime = savedData.getMuteEnd();
        String reason = savedData.getMuteReason();

        if (LocalDateTime.now().isBefore(muteTime)) {

            PlayerUtil.getNexusPlayer(player).sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("You have been muted for ").decoration(ChatFormat.bold, false))
                            .append(Component.text(muteTimeToText(muteTime)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".\nReason: ").decoration(ChatFormat.bold, false))
                            .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
            );
            return true;
        }
        return false;
    }

    private static String muteTimeToText(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, localDateTime);

        return DurationFormatUtils.formatDuration(duration.toMillis(), "d'd', HH'h', mm'm', ss's'", true);
    }

}
