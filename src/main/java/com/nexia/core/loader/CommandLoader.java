package com.nexia.core.loader;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.nexia.core.commands.player.*;
import com.nexia.core.commands.player.duels.*;
import com.nexia.core.commands.player.duels.custom.CustomDuelCommand;
import com.nexia.core.commands.player.duels.custom.KitEditorCommand;
import com.nexia.core.commands.player.ffa.KitCommand;
import com.nexia.core.commands.staff.*;
import com.nexia.core.commands.staff.dev.*;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.commands.minecraft.LinkCommand;
import com.nexia.discord.commands.minecraft.UnLinkCommand;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;

public class CommandLoader {

    public static void registerNexusCommands() {
        CommandDispatcher<CommandSourceInfo> commandDispatcher = ServerTime.nexusServer.getCommandDispatcher();

        SprintFixCommand.register(commandDispatcher);
        HelpCommand.register(commandDispatcher);
        DiscordCommand.register(commandDispatcher);
        LeaveCommand.register(commandDispatcher);
        PrefixCommand.register(commandDispatcher);
        RankCommand.register(commandDispatcher);
        LinkCommand.register(commandDispatcher);
        PingCommand.register(commandDispatcher);
        SpectateCommand.register(commandDispatcher);
        PartyCommand.register(commandDispatcher);
        TempBanCommand.register(commandDispatcher);
        ShoutCommand.register(commandDispatcher);
        RanksCommand.register(commandDispatcher);
        UnTempBanCommand.register(commandDispatcher);
        UnLinkCommand.register(commandDispatcher);
        GamemodeBanCommand.register(commandDispatcher);
        UnGamemodeBanCommand.register(commandDispatcher);
        DeclineDuelCommand.register(commandDispatcher);
        AcceptDuelCommand.register(commandDispatcher);
        StaffPrefixCommand.register(commandDispatcher);
        KitEditorCommand.register(commandDispatcher);
        KitLayoutCommand.register(commandDispatcher);
        CustomDuelCommand.register(commandDispatcher);
        StaffReportCommand.register(commandDispatcher);
        ListInventoriesCommand.register(commandDispatcher);
        DuelCommand.register(commandDispatcher);
        QueueCommand.register(commandDispatcher);
        MapCommand.register(commandDispatcher);
        MuteCommand.register(commandDispatcher);
        UnMuteCommand.register(commandDispatcher);
        PlayCommand.register(commandDispatcher);
        KitCommand.register(commandDispatcher);
        BanCommand.register(commandDispatcher);
        UnBanCommand.register(commandDispatcher);
        StatsCommand.register(commandDispatcher);
        SprintFixCommand.register(commandDispatcher);
        RulesCommand.register(commandDispatcher);
        ReportCommand.register(commandDispatcher);
        SprintFixCommand.register(commandDispatcher);
        SprintFixCommand.register(commandDispatcher);
        MessageCommand.registerMsg(commandDispatcher);
        MessageCommand.registerReply(commandDispatcher);
        HealCommand.register(commandDispatcher);
        DetectCommand.register(commandDispatcher);
    }

    public static void registerCommands() {
        Event<CommandRegistrationCallback> callbackEvent = CommandRegistrationCallback.EVENT;

        callbackEvent.register(ForceGameEndCommand::register);
        callbackEvent.register(ProtectionMapCommand::register);
        callbackEvent.register(DevExperimentalCommandsCommand::register);
        callbackEvent.register(LoadInventoryCommand::register);
        callbackEvent.register(SaveInventoryCommand::register);
        callbackEvent.register(DeleteInventoryCommand::register);
        callbackEvent.register(BwReloadShopCommand::register);
        callbackEvent.register(RandomCommand::register);
        callbackEvent.register(BwReloadTeamColorsCommand::register);
        callbackEvent.register(SkipQueueCommand::register);
    }
}
