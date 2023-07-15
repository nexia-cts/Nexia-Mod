package com.nexia.minigames.games.duels.team;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DuelsTeam {
    public ServerPlayer creator;

    public List<ServerPlayer> people = new ArrayList<>();

    public List<ServerPlayer> alive = new ArrayList<>();

    private List<ServerPlayer> invited = new ArrayList<>();

    public List<ServerPlayer> all = new ArrayList<>();

    public DuelsTeam(ServerPlayer creator, List<ServerPlayer> people){
        this.creator = creator;
        this.people.addAll(people);

        List<ServerPlayer> allPlayers = new ArrayList<>();
        allPlayers.add(creator);
        allPlayers.addAll(people);
        this.all.addAll(allPlayers);
    }


    public boolean refreshCreator(@NotNull ServerPlayer creator) {
        this.creator = ServerTime.minecraftServer.getPlayerList().getPlayer(this.creator.getUUID());
        if(this.creator == null) {
            this.creator = creator;
            this.disbandTeam(creator, false);
        }
        return this.creator.getScoreboardName().equalsIgnoreCase(creator.getScoreboardName());
    }

    public void refreshPeople() {
        List<ServerPlayer> refreshedPlayers = new ArrayList<>();

        for(ServerPlayer player : this.people) {
            refreshedPlayers.add(ServerTime.minecraftServer.getPlayerList().getPlayer(player.getUUID()));
        }

        this.people.clear();
        this.people.addAll(refreshedPlayers);

    }

    public void refreshTeam() {
        refreshCreator(this.creator);
        refreshPeople();

        this.alive.clear();
        this.all.clear();
        this.all.add(this.creator);
        this.all.addAll(this.people);
    }


    public void disbandTeam(ServerPlayer executor, boolean message) {
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor);

        if(!this.refreshCreator(executor)) {
            if(message) factoryExecutor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        TextComponent msg = LegacyChatFormat.format("§7The duels team has been disbanded.");
        for(ServerPlayer player : all) {
            PlayerDataManager.get(player).duelsTeam = null;
            if(message) player.sendMessage(msg, Util.NIL_UUID);
        }
        this.people.clear();
        this.all.clear();
        this.alive.clear();
        this.creator = null;
        this.invited.clear();
        if(message) factoryExecutor.sendMessage(Component.text("You have disbanded your own team.").color(ChatFormat.normalColor));
    }

    public void leaveTeam(ServerPlayer player, boolean message) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        PlayerData data = PlayerDataManager.get(player);
        if(this.refreshCreator(player)) {
            if(message) factoryPlayer.sendMessage(Component.text("You cannot leave your own team without disbanding it!").color(ChatFormat.failColor));
            return;
        }

        data.duelsTeam = null;
        this.people.remove(player);
        this.all.remove(player);
        this.alive.remove(player);

        if(message) factoryPlayer.sendMessage(Component.text("You have left " + this.creator.getScoreboardName() + "'s Team.").color(ChatFormat.normalColor));
        PlayerUtil.broadcast(this.all, LegacyChatFormat.format("§d{} §7has left the team.", player.getScoreboardName()));
    }

    public void invitePlayer(ServerPlayer invitor, ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInvitor = PlayerUtil.getFactoryPlayer(invitor);

        boolean isCreator = this.refreshCreator(invitor);

        if(!isCreator){
            factoryInvitor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        if(player == this.creator) {
            factoryInvitor.sendMessage(Component.text("You cannot invite yourself!").color(ChatFormat.failColor));
            return;
        }


        if(com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode != PlayerGameMode.LOBBY) {
            factoryInvitor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        if(PlayerDataManager.get(player).duelsTeam != null) {
            factoryInvitor.sendMessage(Component.text("That player is already in a team!").color(ChatFormat.failColor));
            return;
        }

        if(this.people.contains(player)) {
            factoryInvitor.sendMessage(Component.text("That player is already in your team!").color(ChatFormat.failColor));
            return;
        }

        PlayerUtil.broadcast(this.all, LegacyChatFormat.format("§d{} §7has been invited to the team.", factoryPlayer.getRawName()));

        this.invited.remove(player);
        this.invited.add(player);

        Component yes = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/party join " + factoryInvitor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );

        Component no = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decoration(ChatFormat.bold, true)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click me")
                                .color(ChatFormat.brandColor2)))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/party decline " + factoryInvitor.getRawName())))
                .append(Component.text("]").color(NamedTextColor.DARK_GRAY)
                );

        factoryPlayer.sendMessage(
                Component.text(factoryInvitor.getRawName()).color(ChatFormat.brandColor2)
                        .append(Component.text(" has invited you to their team!").color(ChatFormat.normalColor))
        );

        factoryPlayer.sendMessage(yes.append(no));
    }

    public void listTeam(ServerPlayer executor) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(executor);

        factoryPlayer.sendMessage(Component.text("People on " + this.creator.getScoreboardName() + "'s Team").color(ChatFormat.normalColor));
        for(ServerPlayer player : this.all) {
            factoryPlayer.sendMessage(Component.text("» ").color(ChatFormat.brandColor1).append(Component.text(player.getScoreboardName()).color(ChatFormat.normalColor)));
        }
    }

    public void kickPlayer(ServerPlayer executor, ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(executor);
        PlayerData data = PlayerDataManager.get(player);

        if(!this.refreshCreator(executor)) {
            factoryInviter.sendMessage(Component.text("You are not the creator!").color(ChatFormat.failColor));
            return;
        }

        if(player == executor) {
            factoryInviter.sendMessage(Component.text("You cannot kick yourself!").color(ChatFormat.failColor));
            return;
        }

        if(!this.people.contains(player)) {
            factoryInviter.sendMessage(Component.text("That player is not in your team!").color(ChatFormat.failColor));
            return;
        }

        PlayerUtil.broadcast(this.all, LegacyChatFormat.format("§d{} §7has been removed from the team.", factoryPlayer.getRawName()));

        data.duelsTeam = null;
        this.people.remove(player);
        this.alive.remove(player);
        this.all.remove(player);

        factoryPlayer.sendMessage(Component.text("You have been kicked from " + factoryInviter.getRawName() + "'s Team.").color(ChatFormat.normalColor));
    }

    public void joinTeam(ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(this.creator);
        PlayerData data = PlayerDataManager.get(player);

        if(data.duelsTeam != null) {
            factoryPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(player)) {
            factoryInviter.sendMessage(Component.text("That player did not invite you!").color(ChatFormat.failColor));
            return;
        }

        this.invited.remove(player);
        this.all.add(player);
        this.people.add(player);

        data.duelsTeam = this;
        factoryPlayer.sendMessage(Component.text("You have joined " + factoryInviter.getRawName() + "'s team").color(ChatFormat.normalColor));
        PlayerUtil.broadcast(this.all, LegacyChatFormat.format("§d{} §7has joined the team.", factoryPlayer.getRawName()));
    }

    public void declineTeam(ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(this.creator);
        PlayerData data = PlayerDataManager.get(player);

        if(data.duelsTeam != null) {
            factoryPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(player)) {
            factoryInviter.sendMessage(Component.text("That player did not invite you!").color(ChatFormat.failColor));
            return;
        }

        this.invited.remove(player);
        factoryPlayer.sendMessage(Component.text("You have declined " + factoryInviter.getRawName() + "'s invite.").color(ChatFormat.normalColor));
        factoryInviter.sendMessage(Component.text(factoryPlayer.getRawName() + " has declined your invite.").color(ChatFormat.failColor));
    }

    public static DuelsTeam createTeam(ServerPlayer player, boolean message) {
        PlayerData data = PlayerDataManager.get(player);
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

        if(data.duelsTeam != null) {
            if(message) {
                factoryPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            }
            return null;
        }

        if(message) {
            factoryPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("You have created a team.")
                            .color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                    )
            );
        }

        DuelsTeam team = new DuelsTeam(player, new ArrayList<>());
        data.duelsTeam = team;

        return team;
    }
}