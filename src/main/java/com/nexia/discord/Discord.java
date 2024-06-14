package com.nexia.discord;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.time.ServerType;
import com.nexia.discord.utilities.discord.DiscordData;
import com.nexia.discord.utilities.discord.DiscordDataManager;
import com.nexia.discord.utilities.player.PlayerData;
import com.nexia.discord.utilities.player.PlayerDataManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class Discord extends ListenerAdapter {
    public static HashMap<Integer, UUID> idMinecraft = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.toLowerCase().contains("link")) {
            ServerType serverType = ServerType.getServerType(event.getOption("server", OptionMapping::getAsString));
            if(serverType != null && serverType != ServerTime.serverType) return;


            User user = event.getUser();
            long discordID = user.getIdLong();
            DiscordData discordData = DiscordDataManager.get(discordID);

            event.deferReply(true).queue();

            if(serverType == null) {
                event.getHook().editOriginal("Invalid server!").queue();
                return;
            }

            if (discordData.savedData.isLinked) {
                event.getHook().editOriginal("You already linked your account!").queue();
                return;
            }

            int code = event.getOption("code", OptionMapping::getAsInt);
            UUID uuid = idMinecraft.get(code);

            if(uuid == null) {
                event.getHook().editOriginal("Invalid code!").queue();
                return;
            }


            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayer(idMinecraft.get(code));

            /*
            if (player == null) {
                event.getHook().editOriginal("Player is not online!").queue();
                return;
            }
             */

            event.deferReply(true).queue();

            PlayerData playerData = PlayerDataManager.get(uuid);

            playerData.savedData.isLinked = true;
            playerData.savedData.discordID = discordID;

            discordData.savedData.isLinked = true;
            discordData.savedData.minecraftUUID = uuid.toString();
            DiscordDataManager.removeDiscordData(discordID);

            idMinecraft.remove(code);
            if(player != null) {
                event.getHook().editOriginal("Your account has been linked with " + player.getScoreboardName()).queue();
                PlayerUtil.getFactoryPlayer(player).sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("Your account has been linked with the discord user: ")
                                        .decoration(ChatFormat.bold, false)
                                        .color(ChatFormat.normalColor)
                                        .append(Component.text("@" + user.getName())
                                                .color(ChatFormat.brandColor1)
                                                .decoration(ChatFormat.bold, true))
                                )
                );
            } else {
                PlayerDataManager.removePlayerData(uuid);
            }
            event.getHook().editOriginal("Your account has been linked!").queue();

        }
    }
}