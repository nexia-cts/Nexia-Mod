package com.nexia.minigames;

import com.combatreforged.metis.api.world.entity.player.Player;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.util.HashMap;

public class GameHandler {

    public static HashMap<ServerPlayer, Integer> countdown = new HashMap<>();

    public static void second() {
        GameHandler.setCountdown();
    }
    public static void showCountdown(ServerPlayer player, int seconds) {
        countdown.put(player, seconds);
    }

    private static void setCountdown() {
        countdown.forEach((player, second) -> {
            int currentSecond = second--;
            if(second <= 0) {
                countdown.remove(player, second);
                return;
            }
            countdown.replace(player, second, currentSecond);

            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            Title title;
            TextColor color = NamedTextColor.GREEN;

            if(currentSecond <= 3 && currentSecond > 1) {
                color = NamedTextColor.YELLOW;
            } else if(currentSecond <= 1) {
                color = NamedTextColor.RED;
            }

            title = Title.title(Component.text(currentSecond).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
            factoryPlayer.sendTitle(title);
        });
    }
}
