package com.nexia.minigames.games.duels.team;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.notcoded.codelib.players.AccuratePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DuelsTeam {
    private AccuratePlayer leader;

    private List<AccuratePlayer> people;

    public List<AccuratePlayer> alive;

    private List<AccuratePlayer> invited;

    public List<AccuratePlayer> all;

    public DuelsTeam(AccuratePlayer leader, List<AccuratePlayer> people){
        this.leader = leader;

        this.people = new ArrayList<>();
        this.people.addAll(people);

        this.alive = new ArrayList<>();
        this.invited = new ArrayList<>();
        this.all = new ArrayList<>();

        List<AccuratePlayer> allPlayers = new ArrayList<>();
        allPlayers.add(leader);
        allPlayers.addAll(people);
        this.all.addAll(allPlayers);
    }


    public AccuratePlayer getLeader() {
        if(this.leader == null || this.leader.get() == null) {
            this.disbandTeam(this.getPeople().get(RandomUtil.randomInt(this.getPeople().size())), false);
        }
        return this.leader;
    }

    public boolean isLeader(AccuratePlayer player) {
        if(player == null || player.get() == null) return false;
        return player.equals(this.getLeader());
    }

    public List<AccuratePlayer> getPeople() {
        return this.people;
    }


    public boolean replaceLeader(@NotNull AccuratePlayer executor, @NotNull AccuratePlayer player, boolean message) {
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor.get());
        
        if(!this.isLeader(executor))  {
            if(message) factoryExecutor.sendMessage(Component.text("You are not the leader!").color(ChatFormat.failColor));
            return false;
        }
        
        if(executor.equals(player) || this.isLeader(player)) {
            if(message) factoryExecutor.sendMessage(Component.text("You are the leader!").color(ChatFormat.failColor));
            return false;
        }

        this.leader = player;
        this.people.remove(player);
        this.people.add(executor);

        if(message) factoryExecutor.sendMessage(Component.text("You have promoted " + player.get().getScoreboardName() + " to the leader."));
        for(AccuratePlayer tPlayer : this.all) {
            tPlayer.get().sendMessage(LegacyChatFormat.format("{s}{} is now the leader.", player.get().getScoreboardName()), Util.NIL_UUID);
        }

        return true;
    }

    public void refreshTeam() {
        this.getLeader();

        this.alive.clear();
        this.all.clear();
        this.all.add(this.leader);
        this.all.addAll(this.people);
    }


    public void disbandTeam(AccuratePlayer executor, boolean message) {
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor.get());

        if(!this.isLeader(AccuratePlayer.create(executor.get()))){
            factoryExecutor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        if(!this.people.isEmpty()) {
            this.leader = this.people.get(RandomUtil.randomInt(this.people.size()));
            this.leaveTeam(executor, true);
            return;
        }

        TextComponent msg = LegacyChatFormat.format("§7The duels team has been disbanded.");
        for(AccuratePlayer player : all) {
            PlayerDataManager.get(player.get()).duelsTeam = null;
            if(message) player.get().sendMessage(msg, Util.NIL_UUID);
        }
        this.people.clear();
        this.all.clear();
        this.alive.clear();
        this.leader = null;
        this.invited.clear();
        if(message) factoryExecutor.sendMessage(Component.text("You have disbanded your own team.").color(ChatFormat.normalColor));
    }

    public void leaveTeam(AccuratePlayer player, boolean message) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player.get());
        PlayerData data = PlayerDataManager.get(player.get());

        if(this.isLeader(player) && this.people.isEmpty()) {
            this.disbandTeam(player, message);
            return;
        }
        if(this.isLeader(player) && !this.people.isEmpty()) this.leader = this.people.get(RandomUtil.randomInt(this.people.size()));


        data.duelsTeam = null;
        this.people.remove(player);
        this.all.remove(player);
        this.alive.remove(player);

        if(message) factoryPlayer.sendMessage(Component.text("You have left " + this.getLeader().get().getScoreboardName() + "'s Team.").color(ChatFormat.normalColor));

        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has left the team.", player.get().getScoreboardName()), Util.NIL_UUID);
        }
    }

    public void invitePlayer(ServerPlayer invitor, ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInvitor = PlayerUtil.getFactoryPlayer(invitor);

        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

        boolean isLeader = this.isLeader(AccuratePlayer.create(invitor));

        if(!isLeader){
            factoryInvitor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        if(player == invitor) {
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

        if(this.people.contains(accuratePlayer)) {
            factoryInvitor.sendMessage(Component.text("That player is already in your team!").color(ChatFormat.failColor));
            return;
        }

        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has been invited to the team.", factoryPlayer.getRawName()), Util.NIL_UUID);
        }

        this.invited.remove(accuratePlayer);
        this.invited.add(accuratePlayer);

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

        factoryPlayer.sendMessage(Component.text("People on " + this.getLeader().get().getScoreboardName() + "'s Team").color(ChatFormat.normalColor));
        for(AccuratePlayer player : this.all) {
            factoryPlayer.sendMessage(Component.text("» ").color(ChatFormat.brandColor1).append(Component.text(player.get().getScoreboardName()).color(ChatFormat.normalColor)));
        }
    }

    public void kickPlayer(ServerPlayer executor, ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(executor);
        PlayerData data = PlayerDataManager.get(player);

        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

        if(!this.isLeader(AccuratePlayer.create(executor))) {
            factoryInviter.sendMessage(Component.text("You are not the leader!").color(ChatFormat.failColor));
            return;
        }

        if(player.equals(executor)) {
            factoryInviter.sendMessage(Component.text("You cannot kick yourself!").color(ChatFormat.failColor));
            return;
        }

        if(!this.people.contains(player)) {
            factoryInviter.sendMessage(Component.text("That player is not in your team!").color(ChatFormat.failColor));
            return;
        }


        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has been removed from the team.", factoryPlayer.getRawName()), Util.NIL_UUID);
        }

        data.duelsTeam = null;
        this.people.remove(accuratePlayer);
        this.alive.remove(accuratePlayer);
        this.all.remove(accuratePlayer);

        factoryPlayer.sendMessage(Component.text("You have been kicked from " + factoryInviter.getRawName() + "'s Team.").color(ChatFormat.normalColor));
    }

    public void joinTeam(ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(this.getLeader().get());
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        PlayerData data = PlayerDataManager.get(player);

        if(data.duelsTeam != null) {
            factoryPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(player)) {
            factoryInviter.sendMessage(Component.text("That player did not invite you!").color(ChatFormat.failColor));
            return;
        }

        this.invited.remove(accuratePlayer);
        this.all.add(accuratePlayer);
        this.people.add(accuratePlayer);

        data.duelsTeam = this;
        factoryPlayer.sendMessage(Component.text("You have joined " + factoryInviter.getRawName() + "'s team").color(ChatFormat.normalColor));
        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has joined the team.", factoryPlayer.getRawName()), Util.NIL_UUID);
        }
    }

    public void declineTeam(ServerPlayer player) {
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
        Player factoryInviter = PlayerUtil.getFactoryPlayer(this.getLeader().get());
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        PlayerData data = PlayerDataManager.get(player);

        if(data.duelsTeam != null) {
            factoryPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(accuratePlayer)) {
            factoryInviter.sendMessage(Component.text("That player did not invite you!").color(ChatFormat.failColor));
            return;
        }

        this.invited.remove(accuratePlayer);
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

        DuelsTeam team = new DuelsTeam(AccuratePlayer.create(player), new ArrayList<>());
        data.duelsTeam = team;

        return team;
    }
}
