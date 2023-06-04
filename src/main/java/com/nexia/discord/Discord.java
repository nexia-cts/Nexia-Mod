package com.nexia.discord;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.utilities.discord.DiscordData;
import com.nexia.discord.utilities.discord.DiscordDataManager;
import com.nexia.discord.utilities.player.PlayerData;
import com.nexia.discord.utilities.player.PlayerDataManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.UUID;

import static com.nexia.discord.Main.jda;

public class Discord extends ListenerAdapter {
    public static HashMap<UUID, Integer> minecraftIDs = new HashMap<UUID, Integer>();
    public static HashMap<Integer, UUID> idMinecraft = new HashMap<Integer, UUID>();

    public static void registerCommands() {
        jda.updateCommands().addCommands(
                Commands.slash("link", "Link the minecraft player with the discord player.")
                        .setGuildOnly(true)
                        .addOption(OptionType.INTEGER, "code", "The code when you do /link in minecraft.", true)
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        com.nexia.core.Main.logger.log(Level.ALL, command);

        if (command.equalsIgnoreCase("link")) {
            event.deferReply().queue();
            DiscordData discordData = DiscordDataManager.get(event.getIdLong());

            if(discordData.savedData.isLinked) {
                event.getHook().editOriginal("You already linked your account!");
                return;
            }


            User user = event.getUser();
            long discordID = user.getIdLong();
            int code = event.getOption("code", OptionMapping::getAsInt);

            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayer(idMinecraft.get(code));
            if(player == null) {
                event.getHook().editOriginal("Invalid code or player is not online!");
                return;
            }
            PlayerData playerData = PlayerDataManager.get(player);

           playerData.savedData.isLinked = true;
           playerData.savedData.discordID = discordID;

           discordData.savedData.isLinked = true;
           discordData.savedData.minecraftUUID = player.getStringUUID();

           minecraftIDs.remove(player.getUUID());
           idMinecraft.remove(code);

            event.getHook().editOriginal("Your account has been linked with " + player.getScoreboardName());
            PlayerUtil.getFactoryPlayer(player).sendMessage(
                    ChatFormat.nexiaMessage()
                            .append(Component.text("Your account has been linked with the discord user id: ")
                                    .decoration(ChatFormat.bold, false)
                                    .color(ChatFormat.normalColor)
                                    .append(Component.text(discordID)
                                            .color(ChatFormat.brandColor1)
                                            .decoration(ChatFormat.bold, true))
                            )
            );


        }
    }
}