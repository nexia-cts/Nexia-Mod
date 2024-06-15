package com.nexia.minigames.games.duels.team;

import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
            if(this.getPeople().isEmpty()) this.disbandTeam(null, false);
            else this.disbandTeam(this.getPeople().get(RandomUtil.randomInt(this.getPeople().size())), false);
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
        Player nexusExecutor = PlayerUtil.getNexusPlayer(executor.get());
        
        if(!this.isLeader(executor))  {
            if(message) nexusExecutor.sendMessage(Component.text("You are not the leader!").color(ChatFormat.failColor));
            return false;
        }
        
        if(executor.equals(player) || this.isLeader(player)) {
            if(message) nexusExecutor.sendMessage(Component.text("You are the leader!").color(ChatFormat.failColor));
            return false;
        }

        this.leader = player;
        this.getPeople().remove(player);
        this.getPeople().add(executor);

        if(message) nexusExecutor.sendMessage(Component.text("You have promoted " + player.get().getScoreboardName() + " to the leader."));
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
        this.all.addAll(this.getPeople());
    }


    public void disbandTeam(AccuratePlayer executor, boolean message) {
        Player nexusExecutor = null;
        if(executor != null && executor.get() != null) nexusExecutor = PlayerUtil.getNexusPlayer(executor.get());

        if(nexusExecutor != null && !this.isLeader(AccuratePlayer.create(executor.get()))){
            nexusExecutor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }



        if(!this.getPeople().isEmpty()) {
            this.leader = this.getPeople().get(RandomUtil.randomInt(this.getPeople().size()));
            if(nexusExecutor != null) this.leaveTeam(executor, true);
            return;
        }

        TextComponent msg = LegacyChatFormat.format("§7The duels team has been disbanded.");
        for(AccuratePlayer player : all) {
            PlayerDataManager.get(player.get()).duelOptions.duelsTeam = null;
            if(message) player.get().sendMessage(msg, Util.NIL_UUID);
        }
        this.getPeople().clear();
        this.all.clear();
        this.alive.clear();
        this.leader = null;
        this.invited.clear();
        if(message && nexusExecutor != null) nexusExecutor.sendMessage(Component.text("You have disbanded your own team.").color(ChatFormat.normalColor));
    }

    public void leaveTeam(AccuratePlayer player, boolean message) {
        Player nexusPlayer = PlayerUtil.getNexusPlayer(player.get());
        PlayerData data = PlayerDataManager.get(player.get());

        if(this.isLeader(player) && this.getPeople().isEmpty()) {
            this.disbandTeam(player, message);
            return;
        }
        if(this.isLeader(player) && !this.getPeople().isEmpty()) this.leader = this.getPeople().get(RandomUtil.randomInt(this.getPeople().size()));


        data.duelOptions.duelsTeam = null;
        this.getPeople().remove(player);
        this.all.remove(player);
        this.alive.remove(player);

        if(message) nexusPlayer.sendMessage(Component.text("You have left " + this.getLeader().get().getScoreboardName() + "'s Team.").color(ChatFormat.normalColor));

        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has left the team.", player.get().getScoreboardName()), Util.NIL_UUID);
        }
    }

    public void invitePlayer(ServerPlayer invitor, ServerPlayer player) {
        Player nexusPlayer = PlayerUtil.getNexusPlayer(player);
        Player nexusInvitor = PlayerUtil.getNexusPlayer(invitor);

        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

        boolean isLeader = this.isLeader(AccuratePlayer.create(invitor));

        if(!isLeader){
            nexusInvitor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        if(player == invitor) {
            nexusInvitor.sendMessage(Component.text("You cannot invite yourself!").color(ChatFormat.failColor));
            return;
        }


        if(com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode != PlayerGameMode.LOBBY) {
            nexusInvitor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        if(PlayerDataManager.get(player).duelOptions.duelsTeam != null) {
            nexusInvitor.sendMessage(Component.text("That player is already in a team!").color(ChatFormat.failColor));
            return;
        }

        if(this.getPeople().contains(accuratePlayer)) {
            nexusInvitor.sendMessage(Component.text("That player is already in your team!").color(ChatFormat.failColor));
            return;
        }

        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has been invited to the team.", nexusPlayer.getRawName()), Util.NIL_UUID);
        }

        this.invited.remove(accuratePlayer);
        this.invited.add(accuratePlayer);

        Component yes = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/party join " + nexusInvitor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );

        Component no = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decoration(ChatFormat.bold, true)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me")
                                .color(ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/party decline " + nexusInvitor.getRawName())))
                .append(Component.text("]").color(NamedTextColor.DARK_GRAY)
                );

        nexusPlayer.sendMessage(
                Component.text(nexusInvitor.getRawName()).color(ChatFormat.brandColor2)
                        .append(Component.text(" has invited you to their team!").color(ChatFormat.normalColor))
        );

        nexusPlayer.sendMessage(yes.append(no));
    }

    public void listTeam(ServerPlayer executor) {
        Player nexusPlayer = PlayerUtil.getNexusPlayer(executor);

        nexusPlayer.sendMessage(Component.text("People on " + this.getLeader().get().getScoreboardName() + "'s Team").color(ChatFormat.normalColor));
        for(AccuratePlayer player : this.all) {
            nexusPlayer.sendMessage(Component.text("» ").color(ChatFormat.brandColor1).append(Component.text(player.get().getScoreboardName()).color(ChatFormat.normalColor)));
        }
    }

    public void kickPlayer(ServerPlayer executor, ServerPlayer player) {
        Player nexusPlayer = PlayerUtil.getNexusPlayer(player);
        Player nexusInviter = PlayerUtil.getNexusPlayer(executor);
        PlayerData data = PlayerDataManager.get(player);

        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

        if(!this.isLeader(AccuratePlayer.create(executor))) {
            nexusInviter.sendMessage(Component.text("You are not the leader!").color(ChatFormat.failColor));
            return;
        }

        if(player.equals(executor)) {
            nexusInviter.sendMessage(Component.text("You cannot kick yourself!").color(ChatFormat.failColor));
            return;
        }

        if(!this.getPeople().contains(accuratePlayer)) {
            nexusInviter.sendMessage(Component.text("That player is not in your team!").color(ChatFormat.failColor));
            return;
        }


        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has been removed from the team.", nexusPlayer.getRawName()), Util.NIL_UUID);
        }

        data.duelOptions.duelsTeam = null;
        this.getPeople().remove(accuratePlayer);
        this.alive.remove(accuratePlayer);
        this.all.remove(accuratePlayer);

        nexusPlayer.sendMessage(Component.text("You have been kicked from " + nexusInviter.getRawName() + "'s Team.").color(ChatFormat.normalColor));
    }

    public void joinTeam(ServerPlayer player) {
        Player nexusPlayer = PlayerUtil.getNexusPlayer(player);
        Player nexusInviter = PlayerUtil.getNexusPlayer(this.getLeader().get());
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        PlayerData data = PlayerDataManager.get(player);

        if(data.duelOptions.duelsTeam != null) {
            nexusPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(accuratePlayer)) {
            nexusInviter.sendMessage(Component.text("That player did not invite you!").color(ChatFormat.failColor));
            return;
        }

        this.invited.remove(accuratePlayer);
        this.all.add(accuratePlayer);
        this.getPeople().add(accuratePlayer);

        data.duelOptions.duelsTeam = this;
        nexusPlayer.sendMessage(Component.text("You have joined " + nexusInviter.getRawName() + "'s team").color(ChatFormat.normalColor));
        for(AccuratePlayer ap : this.all) {
            ap.get().sendMessage(LegacyChatFormat.format("§d{} §7has joined the team.", nexusPlayer.getRawName()), Util.NIL_UUID);
        }
    }

    public void declineTeam(ServerPlayer player) {
        Player nexusPlayer = PlayerUtil.getNexusPlayer(player);
        Player nexusInviter = PlayerUtil.getNexusPlayer(this.getLeader().get());
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        PlayerData data = PlayerDataManager.get(player);

        if(data.duelOptions.duelsTeam != null) {
            nexusPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(accuratePlayer)) {
            nexusInviter.sendMessage(Component.text("That player did not invite you!").color(ChatFormat.failColor));
            return;
        }

        this.invited.remove(accuratePlayer);
        nexusPlayer.sendMessage(Component.text("You have declined " + nexusInviter.getRawName() + "'s invite.").color(ChatFormat.normalColor));
        nexusInviter.sendMessage(Component.text(nexusPlayer.getRawName() + " has declined your invite.").color(ChatFormat.failColor));
    }

    public static DuelsTeam createTeam(ServerPlayer player, boolean message) {
        PlayerData data = PlayerDataManager.get(player);
        Player nexusPlayer = PlayerUtil.getNexusPlayer(player);

        if(data.duelOptions.duelsTeam != null) {
            if(message) {
                nexusPlayer.sendMessage(Component.text("You are currently in a team!").color(ChatFormat.failColor));
            }
            return null;
        }

        if(message) {
            nexusPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("You have created a team.")
                            .color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                    )
            );
        }

        DuelsTeam team = new DuelsTeam(AccuratePlayer.create(player), new ArrayList<>());
        data.duelOptions.duelsTeam = team;

        return team;
    }
}
