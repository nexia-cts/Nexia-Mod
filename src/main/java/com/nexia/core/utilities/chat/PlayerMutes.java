package com.nexia.core.utilities.chat;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.base.player.NexiaPlayer;
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

        CoreSavedPlayerData mutedData = (CoreSavedPlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(muted.getUUID()).savedData;
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

        new NexiaPlayer(muted).sendNexiaMessage(
                Component.text("You have been muted for ", ChatFormat.normalColor)
                        .append(Component.text(muteTimeToText(mutedData.getMuteEnd()), ChatFormat.brandColor2))
                        .append(Component.text(".\nReason: ", ChatFormat.normalColor))
                        .append(Component.text(reason, ChatFormat.brandColor2))
        );

    }

    public static void unMute(CommandSourceInfo sender, ServerPlayer unMuted) {
        CoreSavedPlayerData unMutedData = (CoreSavedPlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(unMuted.getUUID()).savedData;
        LocalDateTime currentMuteTime = unMutedData.getMuteEnd();

        if (LocalDateTime.now().isAfter(currentMuteTime)) {
            sender.sendMessage(ChatFormat.nexiaMessage.append(Component.text("This player is not muted.", ChatFormat.normalColor)));
            return;
        }

        unMutedData.setMuteEnd(LocalDateTime.MIN);
        unMutedData.setMuteReason(null);

        sender.sendMessage(
                ChatFormat.nexiaMessage.append(
                        Component.text("Unmuted ", ChatFormat.normalColor)
                                .append(Component.text(unMuted.getScoreboardName(), ChatFormat.brandColor2))
                                .append(Component.text(".", ChatFormat.normalColor))
                )
        );

        new NexiaPlayer(unMuted).sendNexiaMessage("You have been unmuted.");
    }

    public static boolean muted(NexiaPlayer player) {
        CoreSavedPlayerData savedData = (CoreSavedPlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player).savedData;
        LocalDateTime muteTime = savedData.getMuteEnd();
        String reason = savedData.getMuteReason();

        if (LocalDateTime.now().isBefore(muteTime)) {

            player.sendNexiaMessage(Component.text("You have been muted for ", ChatFormat.normalColor)
                            .append(Component.text(muteTimeToText(muteTime), ChatFormat.brandColor2))
                            .append(Component.text(".\nReason: "))
                            .append(Component.text(reason, ChatFormat.brandColor2))
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
