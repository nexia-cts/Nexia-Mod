package com.nexia.discord.commands.minecraft;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.discord.Discord;
import com.nexia.discord.Main;
import com.nexia.discord.utilities.discord.DiscordData;
import com.nexia.discord.utilities.discord.DiscordDataManager;
import com.nexia.discord.utilities.player.PlayerData;
import com.nexia.discord.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameMode;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static com.nexia.discord.Main.jda;

public class UnLinkCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("unlink")
                .requires(commandSourceStack -> {
                    try {
                        return PlayerDataManager.get(commandSourceStack.getPlayerOrException()).savedData.isLinked;
                    } catch (Exception ignored) { }
                    return false;
                })
                .executes(UnLinkCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

        PlayerData data = PlayerDataManager.get(player);
        Member user = jda.getGuildById(Main.config.guildID).retrieveMemberById(data.savedData.discordID).complete();

        data.savedData.isLinked = false;
        data.savedData.discordID = 0;

        if(user != null) {
            DiscordData discordData = DiscordDataManager.get(user.getIdLong());
            discordData.savedData.isLinked = false;
            discordData.savedData.minecraftUUID = "";
        }

        factoryPlayer.sendMessage(
                ChatFormat.nexiaMessage()
                        .append(Component.text("You have successfully unlinked your discord account.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        return Command.SINGLE_SUCCESS;
    }

}

