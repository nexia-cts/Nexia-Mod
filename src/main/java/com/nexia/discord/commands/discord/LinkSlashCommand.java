package com.nexia.discord.commands.discord;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.time.ServerType;
import com.nexia.discord.commands.discord.base.BaseSlashCommand;
import com.nexia.discord.utilities.discord.DiscordData;
import com.nexia.discord.utilities.discord.DiscordDataManager;
import com.nexia.nexus.api.world.entity.player.Player;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.UUID;

public class LinkSlashCommand extends BaseSlashCommand {
    public static HashMap<Integer, UUID> idMinecraft = new HashMap<>();

    public LinkSlashCommand() {
        super("link", "Link the minecraft player with the discord player.",
                new OptionData(OptionType.STRING, "server", "Which server you're trying to link on.")
                        .setRequired(true)
                        .setMaxLength(3)
                        .addChoice("EU", "eu")
                        .addChoice("NA", "na")
                        .addChoice("DEV", "dev"),
                new OptionData(OptionType.INTEGER, "code", "The code when you do /link in Minecraft.")
                        .setRequired(true)
                        .setRequiredRange(1000, 9999)
        );
    }

    @Override
    public void commandTriggered(SlashCommandInteractionEvent event) {
        ServerType serverType = ServerType.getServerType(event.getOption("server", OptionMapping::getAsString));
        if(serverType != null && serverType != ServerTime.serverType) return;

        User user = event.getUser();
        long discordID = user.getIdLong();
        DiscordData discordData = DiscordDataManager.get(discordID);

        if(serverType == null) {
            this.sendMessage("Invalid server!", event);
            return;
        }

        if (discordData.savedData.isLinked) {
            this.sendMessage("You already linked your account!", event);
            return;
        }

        int code = event.getOption("code", OptionMapping::getAsInt);
        UUID uuid = idMinecraft.get(code);

        if(uuid == null) {
            this.sendMessage("Invalid code!", event);
            return;
        }


        Player player = ServerTime.nexusServer.getPlayer(idMinecraft.get(code));

        if (player == null) {
            this.sendMessage("The player is not online! Please try again after joining the server.", event);
            DiscordDataManager.removeDiscordData(discordID);
            return;
        }

        PlayerData playerData = PlayerDataManager.getDataManager(NexiaCore.DISCORD_DATA_MANAGER).get(uuid);

        playerData.savedData.set(Boolean.class, "isLinked", true);
        playerData.savedData.set(Long.class, "discordID", discordID);

        discordData.savedData.isLinked = true;
        discordData.savedData.minecraftUUID = uuid.toString();
        DiscordDataManager.removeDiscordData(discordID);

        idMinecraft.remove(code);
        this.sendMessage(String.format("Your account has been linked with `%s`.", player.getRawName()), event);

        player.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("Your account has been linked with the discord user: ", ChatFormat.normalColor)
                                .append(Component.text("@" + user.getName())
                                        .color(ChatFormat.brandColor1)
                                        .decoration(ChatFormat.bold, true))
                        )
        );

        PlayerDataManager.getDataManager(NexiaCore.DISCORD_DATA_MANAGER).removePlayerData(uuid);
    }
}
