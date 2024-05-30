package com.nexia.discord.commands.minecraft;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.discord.Main;
import com.nexia.discord.utilities.player.PlayerData;
import com.nexia.discord.utilities.player.PlayerDataManager;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;

import java.io.File;

import static com.nexia.discord.Main.jda;

public class UnLinkCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("unlink")
                .requires(commandSourceInfo -> {
                    try {
                        if(!CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                        return PlayerDataManager.get(CommandUtil.getPlayer(commandSourceInfo).getUUID()).savedData.isLinked;
                    } catch (Exception ignored) { }
                    return false;
                })
                .executes(UnLinkCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = CommandUtil.getPlayer(context);

        PlayerData data = PlayerDataManager.get(player.getUUID());

        data.savedData.isLinked = false;

        try {
            jda.getGuildById(Main.config.guildID).retrieveMemberById(data.savedData.discordID).complete();
        } catch (NullPointerException exception) {
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
