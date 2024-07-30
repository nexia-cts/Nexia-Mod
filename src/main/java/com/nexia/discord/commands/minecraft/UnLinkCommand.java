package com.nexia.discord.commands.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerData;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.discord.NexiaDiscord;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

import static com.nexia.discord.NexiaDiscord.jda;

public class UnLinkCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("unlink")
                .requires(commandSourceInfo -> {
                    try {
                        return PlayerDataManager.getDataManager(NexiaCore.DISCORD_DATA_MANAGER).get(commandSourceInfo.getPlayerOrException().getUUID()).savedData.get(Boolean.class, "isLinked");
                    } catch (Exception ignored) { return false; }

                })
                .executes(UnLinkCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());
        PlayerData data = PlayerDataManager.getDataManager(NexiaCore.DISCORD_DATA_MANAGER).get(player.getUUID());

        data.savedData.set(Boolean.class, "isLinked", false);
        long discordID = data.savedData.get(Long.class, "discordID");

        try {
            jda.getGuildById(NexiaDiscord.config.guildID).retrieveMemberById(discordID).complete();
        } catch (Exception exception) {
            new File(FabricLoader.getInstance().getConfigDir().toString() + "/nexia/discord/discorddata", discordID + ".json").delete();
        }

        if(discordID != 0){
            new File(FabricLoader.getInstance().getConfigDir().toString() + "/nexia/discord/discorddata", discordID + ".json").delete();
        }

        data.savedData.set(Long.class, "discordID", 0L);

        player.sendNexiaMessage("You have successfully unlinked your discord account.");
        return 1;
    }

}
