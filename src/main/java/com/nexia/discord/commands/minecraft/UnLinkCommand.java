package com.nexia.discord.commands.minecraft;

import com.nexia.nexus.api.world.entity.player.Player;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.discord.Main;
import com.nexia.discord.utilities.player.PlayerData;
import com.nexia.discord.utilities.player.PlayerDataManager;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;

import static com.nexia.discord.Main.jda;

public class UnLinkCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("unlink")
                .requires(commandSourceStack -> {
                    try {
                        return PlayerDataManager.get(commandSourceStack.getPlayerOrException().getUUID()).savedData.isLinked;
                    } catch (Exception ignored) { }
                    return false;
                })
                .executes(UnLinkCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Player nexusPlayer = PlayerUtil.getNexusPlayer(player);

        PlayerData data = PlayerDataManager.get(player.getUUID());

        data.savedData.isLinked = false;

        Member user = null;
        try {
            jda.getGuildById(Main.config.guildID).retrieveMemberById(data.savedData.discordID).complete();
        } catch (NullPointerException exception) {
            new File(FabricLoader.getInstance().getConfigDir().toString() + "/nexia/discord/discorddata", data.savedData.discordID + ".json").delete();
        }

        if(data.savedData.discordID != 0){
            new File(FabricLoader.getInstance().getConfigDir().toString() + "/nexia/discord/discorddata", data.savedData.discordID + ".json").delete();
        }

        data.savedData.discordID = 0;

        nexusPlayer.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You have successfully unlinked your discord account.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        return Command.SINGLE_SUCCESS;
    }

}
