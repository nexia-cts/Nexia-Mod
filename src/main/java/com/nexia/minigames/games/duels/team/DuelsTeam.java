package com.nexia.minigames.games.duels.team;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        this.alive.addAll(allPlayers);
    }

    public void disbandTeam(ServerPlayer executor, boolean message) {

        if(executor != this.creator) {
            if(message) PlayerUtil.getFactoryPlayer(executor).sendMessage(Component.text("You are not the creator!").color(ChatFormat.failColor));
            return;
        }

        TextComponent msg = LegacyChatFormat.format("§7The duels team has been disbanded.");
        for(ServerPlayer player : all) {
            PlayerDataManager.get(player).duelsTeam = null;
            if(message) player.sendMessage(msg, Util.NIL_UUID);
        }
        this.people.clear();
        this.all.clear();
        this.creator = null;
        this.invited.clear();
    }

    public void leaveTeam(ServerPlayer player, boolean message) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        PlayerData data = PlayerDataManager.get(player);
        if(this.creator == player) {
            if(message) factoryPlayer.sendMessage(Component.text("You cannot leave your own team without disbanding it!").color(ChatFormat.failColor));
            return;
        }

        data.duelsTeam = null;
        this.people.remove(player);
        this.all.remove(player);
        PlayerUtil.broadcast(this.all, LegacyChatFormat.format("§d{} §7has left the team.", player.getScoreboardName()));
    }

    public void invitePlayer(ServerPlayer invitor, ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(this.creator);
        if(player == this.creator) {
            factoryPlayer.sendMessage(Component.text("You cannot invite yourself!").color(ChatFormat.failColor));
            return;
        }
        if(invitor != this.creator){
            factoryPlayer.sendMessage(Component.text("You are not the creator!").color(ChatFormat.failColor));
            return;
        }

        if(this.people.contains(player)) {
            factoryInviter.sendMessage(Component.text("That player is already in your team!").color(ChatFormat.failColor));
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
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/team join " + factoryInviter.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );

        Component no = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decoration(ChatFormat.bold, true)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click me")
                                .color(ChatFormat.brandColor2)))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/team decline " + factoryInviter.getRawName())))
                .append(Component.text("]").color(NamedTextColor.DARK_GRAY)
                );

        factoryPlayer.sendMessage(
                Component.text(factoryInviter.getRawName()).color(ChatFormat.brandColor2)
                        .append(Component.text(" has invited you to their team!").color(ChatFormat.normalColor))
        );

        factoryPlayer.sendMessage(yes.append(no));
    }

    public void kickPlayer(ServerPlayer executor, ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(this.creator);
        PlayerData data = PlayerDataManager.get(player);

        if(player == this.creator) {
            factoryPlayer.sendMessage(Component.text("You cannot kick yourself!").color(ChatFormat.failColor));
            return;
        }
        if(executor != this.creator) {
            factoryPlayer.sendMessage(Component.text("You are not the creator!").color(ChatFormat.failColor));
            return;
        }

        if(!this.people.contains(player)) {
            factoryInviter.sendMessage(Component.text("That player is not in your team!").color(ChatFormat.failColor));
            return;
        }

        PlayerUtil.broadcast(this.all, LegacyChatFormat.format("§d{} §7has been removed from the team.", factoryPlayer.getRawName()));

        data.duelsTeam = null;
        this.people.remove(player);
        this.all.remove(player);
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
        PlayerUtil.broadcast(this.all, LegacyChatFormat.format("§d{} §7has joined team.", factoryPlayer.getRawName()));
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
