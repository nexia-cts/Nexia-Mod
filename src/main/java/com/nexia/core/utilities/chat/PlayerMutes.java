package com.nexia.core.utilities.chat;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.CoreSavedPlayerData;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalDateTime;

public class PlayerMutes {

    public static void mute(CommandSourceInfo sender, ServerPlayer muted, long duration, String reason) {
        if (Permissions.check(muted, "nexia.staff.mute", 1)) {
            sender.sendMessage(Component.text("You can't mute staff.", ChatFormat.failColor));
            return;
        }

        CoreSavedPlayerData mutedData = PlayerDataManager.get(muted.getUUID()).savedData;
        LocalDateTime currentMuteTime = mutedData.getMuteEnd();

        if (LocalDateTime.now().isBefore(currentMuteTime)) {
            sender.sendMessage(Component.text("This player has already been muted for ", ChatFormat.systemColor)
                    .append(Component.text(muteTimeToText(currentMuteTime), ChatFormat.failColor))
                    .append(Component.text(".", ChatFormat.systemColor))
            );
            sender.sendMessage(Component.text("Reason: ", ChatFormat.systemColor)
                    .append(Component.text(mutedData.getMuteReason(), ChatFormat.failColor))
            );
            return;
        }

        mutedData.setMuteEnd(LocalDateTime.now().plusSeconds(duration));
        mutedData.setMuteReason(reason);


        sender.sendMessage(Component.text("Muted ", ChatFormat.systemColor)
                .append(Component.text(muted.getScoreboardName(), ChatFormat.brandColor2))
                .append(Component.text(" for ", ChatFormat.systemColor))
                .append(Component.text(muteTimeToText(mutedData.getMuteEnd()), ChatFormat.brandColor2))
                .append(Component.text(".", ChatFormat.systemColor))
        );

        sender.sendMessage(Component.text("Reason: ", ChatFormat.systemColor)
                .append(Component.text(mutedData.getMuteReason(), ChatFormat.brandColor2))
        );

        new NexiaPlayer(muted).sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been muted for ").decoration(ChatFormat.bold, false))
                                        .append(Component.text(muteTimeToText(mutedData.getMuteEnd())).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                .append(Component.text(".\nReason: ").decoration(ChatFormat.bold, false))
                                                        .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
        );

    }

    public static void unMute(CommandSourceInfo sender, ServerPlayer unMuted) {
        CoreSavedPlayerData unMutedData = PlayerDataManager.get(unMuted.getUUID()).savedData;
        LocalDateTime currentMuteTime = unMutedData.getMuteEnd();

        if (LocalDateTime.now().isAfter(currentMuteTime)) {
            sender.sendMessage(Component.text("This player is not muted.", ChatFormat.systemColor));
            return;
        }

        unMutedData.setMuteEnd(LocalDateTime.MIN);
        unMutedData.setMuteReason(null);

        sender.sendMessage(Component.text("Unmuted ", ChatFormat.systemColor)
                .append(Component.text(unMuted.getScoreboardName(), ChatFormat.brandColor2))
                .append(Component.text(".", ChatFormat.systemColor))
        );

        new NexiaPlayer(unMuted).sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been unmuted.").decoration(ChatFormat.bold, false))
        );
    }

    public static boolean muted(NexiaPlayer player) {
        CoreSavedPlayerData savedData = PlayerDataManager.get(player).savedData;
        LocalDateTime muteTime = savedData.getMuteEnd();
        String reason = savedData.getMuteReason();

        if (LocalDateTime.now().isBefore(muteTime)) {

            player.sendMessage(
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
