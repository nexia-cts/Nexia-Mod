package com.nexia.core.loader;

import com.nexia.core.commands.player.*;
import com.nexia.core.commands.player.duels.*;
import com.nexia.core.commands.player.duels.custom.CustomDuelCommand;
import com.nexia.core.commands.player.duels.custom.KitEditorCommand;
import com.nexia.core.commands.player.ffa.BiomeCommand;
import com.nexia.core.commands.player.ffa.KitCommand;
import com.nexia.core.commands.staff.*;
import com.nexia.core.commands.staff.dev.*;
import com.nexia.discord.commands.minecraft.LinkCommand;
import com.nexia.discord.commands.minecraft.UnLinkCommand;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;

public class CommandLoader {

    public static void registerCommands() {
        Event<CommandRegistrationCallback> callbackEvent = CommandRegistrationCallback.EVENT;

        callbackEvent.register(SprintFixCommand::register);
        callbackEvent.register(DiscordCommand::register);
        callbackEvent.register(LeaveCommand::register);
        callbackEvent.register(ProtectionMapCommand::register);
        callbackEvent.register(PrefixCommand::register);
        callbackEvent.register(DetectCommand::register);
        callbackEvent.register(RankCommand::register);
        callbackEvent.register(LinkCommand::register);
        callbackEvent.register(PingCommand::register);
        callbackEvent.register(ForceGameEndCommand::register);
        callbackEvent.register(SpectateCommand::register);
        callbackEvent.register(PartyCommand::register);
        callbackEvent.register(TempBanCommand::register);
        callbackEvent.register(ShoutCommand::register);
        callbackEvent.register(RanksCommand::register);
        callbackEvent.register(UnTempBanCommand::register);
        callbackEvent.register(UnLinkCommand::register);
        callbackEvent.register(GamemodeBanCommand::register);
        callbackEvent.register(UnGamemodeBanCommand::register);
        callbackEvent.register(AcceptDuelCommand::register);
        callbackEvent.register(DeclineDuelCommand::register);
        callbackEvent.register(StaffPrefixCommand::register);
        callbackEvent.register(DevExperimentalMapCommand::register);
        callbackEvent.register(LoadInventoryCommand::register);
        callbackEvent.register(SaveInventoryCommand::register);
        callbackEvent.register(ListInventoriesCommand::register);
        callbackEvent.register(DeleteInventoryCommand::register);
        callbackEvent.register(KitEditorCommand::register);
        callbackEvent.register(KitLayoutCommand::register);
        callbackEvent.register(CustomDuelCommand::register);
        callbackEvent.register(StaffReportCommand::register);
        callbackEvent.register(DuelCommand::register);
        callbackEvent.register(QueueCommand::register);
        callbackEvent.register(MapCommand::register);
        callbackEvent.register(BiomeCommand::register);
        callbackEvent.register(MuteCommand::register);
        callbackEvent.register(UnMuteCommand::register);
        callbackEvent.register(PlayCommand::register);
        callbackEvent.register(KitCommand::register);
        callbackEvent.register(BanCommand::register);
        callbackEvent.register(UnBanCommand::register);
        callbackEvent.register(StatsCommand::register);
        callbackEvent.register(RulesCommand::register);
        callbackEvent.register(HelpCommand::register);
        callbackEvent.register(ReportCommand::register);
        callbackEvent.register(MessageCommand::registerMsg);
        callbackEvent.register(MessageCommand::registerReply);
        callbackEvent.register(RandomCommand::register);
        callbackEvent.register(HealCommand::register);
        callbackEvent.register(BwReloadShopCommand::register);
        callbackEvent.register(BwReloadTeamColorsCommand::register);
        callbackEvent.register(SkipQueueCommand::register);
    }
}
