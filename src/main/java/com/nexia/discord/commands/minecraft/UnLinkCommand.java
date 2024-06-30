package com.nexia.discord.commands.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.discord.Main;
import com.nexia.discord.utilities.player.PlayerData;
import com.nexia.discord.utilities.player.PlayerDataManager;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;

import java.io.File;

import static com.nexia.discord.Main.jda;

public class UnLinkCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("unlink")
                .requires(commandSourceInfo -> {
                    try {
                        return PlayerDataManager.get(commandSourceInfo.getPlayerOrException().getUUID()).savedData.isLinked;
                    } catch (Exception ignored) { }
                    return false;
                })
                .executes(UnLinkCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

        PlayerData data = PlayerDataManager.get(player.getUUID());

        data.savedData.isLinked = false;

        try {
            jda.getGuildById(Main.config.guildID).retrieveMemberById(data.savedData.discordID).complete();
        } catch (Exception exception) {
            new File(FabricLoader.getInstance().getConfigDir().toString() + "/nexia/discord/discorddata", data.savedData.discordID + ".json").delete();
        }

        if(data.savedData.discordID != 0){
            new File(FabricLoader.getInstance().getConfigDir().toString() + "/nexia/discord/discorddata", data.savedData.discordID + ".json").delete();
        }

        data.savedData.discordID = 0;

        player.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You have successfully unlinked your discord account.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        return 1;
    }

}
