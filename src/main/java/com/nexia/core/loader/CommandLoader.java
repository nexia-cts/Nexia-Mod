package com.nexia.core.loader;

import com.mojang.brigadier.tree.CommandNode;
import com.nexia.core.commands.player.*;
import com.nexia.core.commands.player.duels.*;
import com.nexia.core.commands.player.oitc.OitcCommand;
import com.nexia.core.commands.staff.*;
import com.nexia.core.utilities.misc.NxCmdUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;

public class CommandLoader {

    public static void registerCommands() {

        /*
        CommandRegistrationCallback.EVENT.register((dispatcher, bl) -> {
            for (CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
                NxCmdUtil.alterCommand(node);
            }
        });

         */

        CommandRegistrationCallback.EVENT.register(DefaultGameRulesCommand::register);
        CommandRegistrationCallback.EVENT.register(ProtectionMapCommand::register);
        CommandRegistrationCallback.EVENT.register(PrefixCommand::register);
        CommandRegistrationCallback.EVENT.register(RankCommand::register);
        CommandRegistrationCallback.EVENT.register(RoundPosCommand::register);
        CommandRegistrationCallback.EVENT.register(PingCommand::register);


        CommandRegistrationCallback.EVENT.register(DuelCommand::register);
        CommandRegistrationCallback.EVENT.register(QueueCommand::register);
        CommandRegistrationCallback.EVENT.register(MapCommand::register);

        CommandRegistrationCallback.EVENT.register(OitcCommand::register);

        CommandRegistrationCallback.EVENT.register(MuteCommand::register);
        CommandRegistrationCallback.EVENT.register(UnMuteCommand::register);

        CommandRegistrationCallback.EVENT.register(PlayCommand::register);
        CommandRegistrationCallback.EVENT.register(LeaveCommand::register);

        CommandRegistrationCallback.EVENT.register(BanCommand::register);
        CommandRegistrationCallback.EVENT.register(UnBanCommand::register);
        CommandRegistrationCallback.EVENT.register(StatsCommand::register);
        CommandRegistrationCallback.EVENT.register(RulesCommand::register);
        CommandRegistrationCallback.EVENT.register(DiscordCommand::register);
        CommandRegistrationCallback.EVENT.register(HelpCommand::register);
        CommandRegistrationCallback.EVENT.register(ReportCommand::register);

        CommandRegistrationCallback.EVENT.register(MessageCommand::registerMsg);
        CommandRegistrationCallback.EVENT.register(MessageCommand::registerReply);

        CommandRegistrationCallback.EVENT.register(RandomCommand::register);

        CommandRegistrationCallback.EVENT.register(HealCommand::register);
        CommandRegistrationCallback.EVENT.register(BwReloadShopCommand::register);
        CommandRegistrationCallback.EVENT.register(BwReloadTeamColorsCommand::register);
        CommandRegistrationCallback.EVENT.register(BwSkipQueueCommand::register);
    }
}
