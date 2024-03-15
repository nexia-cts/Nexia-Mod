package com.nexia.core.loader;

import com.nexia.core.commands.player.*;
import com.nexia.core.commands.player.duels.*;
import com.nexia.core.commands.player.duels.custom.CustomDuelCommand;
import com.nexia.core.commands.player.duels.custom.KitEditorCommand;
import com.nexia.core.commands.player.ffa.*;
import com.nexia.core.commands.staff.*;
import com.nexia.core.commands.staff.dev.*;
import com.nexia.discord.commands.minecraft.*;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class CommandLoader {

    public static void registerCommands() {

        CommandRegistrationCallback.EVENT.register(DiscordCommand::register);
        CommandRegistrationCallback.EVENT.register(LeaveCommand::register);


        CommandRegistrationCallback.EVENT.register(ProtectionMapCommand::register);
        CommandRegistrationCallback.EVENT.register(PrefixCommand::register);
        CommandRegistrationCallback.EVENT.register(DetectCommand::register);
        CommandRegistrationCallback.EVENT.register(RankCommand::register);
        CommandRegistrationCallback.EVENT.register(LinkCommand::register);
        CommandRegistrationCallback.EVENT.register(PingCommand::register);

        CommandRegistrationCallback.EVENT.register(ForceGameEndCommand::register);

        CommandRegistrationCallback.EVENT.register(SpectateCommand::register);
        CommandRegistrationCallback.EVENT.register(TeamCommand::register);

        CommandRegistrationCallback.EVENT.register(TempBanCommand::register);
        CommandRegistrationCallback.EVENT.register(ShoutCommand::register);
        CommandRegistrationCallback.EVENT.register(RanksCommand::register);
        CommandRegistrationCallback.EVENT.register(UnTempBanCommand::register);
        CommandRegistrationCallback.EVENT.register(UnLinkCommand::register);

        CommandRegistrationCallback.EVENT.register(GamemodeBanCommand::register);
        CommandRegistrationCallback.EVENT.register(UnGamemodeBanCommand::register);

        CommandRegistrationCallback.EVENT.register(AcceptDuelCommand::register);
        CommandRegistrationCallback.EVENT.register(DeclineDuelCommand::register);

        CommandRegistrationCallback.EVENT.register(StaffPrefixCommand::register);
        CommandRegistrationCallback.EVENT.register(DevExperimentalMapCommand::register);

        CommandRegistrationCallback.EVENT.register(LoadInventoryCommand::register);
        CommandRegistrationCallback.EVENT.register(SaveInventoryCommand::register);
        CommandRegistrationCallback.EVENT.register(ListInventoriesCommand::register);
        CommandRegistrationCallback.EVENT.register(DeleteInventoryCommand::register);

        CommandRegistrationCallback.EVENT.register(KitEditorCommand::register);
        CommandRegistrationCallback.EVENT.register(CustomDuelCommand::register);

        CommandRegistrationCallback.EVENT.register(StaffReportCommand::register);

        CommandRegistrationCallback.EVENT.register(DuelCommand::register);
        CommandRegistrationCallback.EVENT.register(QueueCommand::register);
        CommandRegistrationCallback.EVENT.register(MapCommand::register);

        CommandRegistrationCallback.EVENT.register(BiomeCommand::register);

        CommandRegistrationCallback.EVENT.register(MuteCommand::register);
        CommandRegistrationCallback.EVENT.register(UnMuteCommand::register);

        CommandRegistrationCallback.EVENT.register(PlayCommand::register);
        CommandRegistrationCallback.EVENT.register(KitCommand::register);

        CommandRegistrationCallback.EVENT.register(BanCommand::register);
        CommandRegistrationCallback.EVENT.register(UnBanCommand::register);
        CommandRegistrationCallback.EVENT.register(StatsCommand::register);
        CommandRegistrationCallback.EVENT.register(RulesCommand::register);
        CommandRegistrationCallback.EVENT.register(HelpCommand::register);
        CommandRegistrationCallback.EVENT.register(ReportCommand::register);

        CommandRegistrationCallback.EVENT.register(MessageCommand::registerMsg);
        CommandRegistrationCallback.EVENT.register(MessageCommand::registerReply);

        CommandRegistrationCallback.EVENT.register(RandomCommand::register);

        CommandRegistrationCallback.EVENT.register(HealCommand::register);
        CommandRegistrationCallback.EVENT.register(BwReloadShopCommand::register);
        CommandRegistrationCallback.EVENT.register(BwReloadTeamColorsCommand::register);
        CommandRegistrationCallback.EVENT.register(SkipQueueCommand::register);
    }
}
