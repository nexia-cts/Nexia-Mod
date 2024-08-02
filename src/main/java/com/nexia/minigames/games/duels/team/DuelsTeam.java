package com.nexia.minigames.games.duels.team;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DuelsTeam {
    private NexiaPlayer leader;

    private List<NexiaPlayer> people;

    public List<NexiaPlayer> alive;

    private List<NexiaPlayer> invited;

    public List<NexiaPlayer> all;

    public DuelsTeam(NexiaPlayer leader, List<NexiaPlayer> people){
        this.leader = leader;

        this.people = new ArrayList<>();
        this.people.addAll(people);

        this.alive = new ArrayList<>();
        this.invited = new ArrayList<>();
        this.all = new ArrayList<>();

        List<NexiaPlayer> allPlayers = new ArrayList<>();
        allPlayers.add(leader);
        allPlayers.addAll(people);
        this.all.addAll(allPlayers);
    }


    public NexiaPlayer getLeader() {
        if(this.leader == null || this.leader.unwrap() == null) {
            if(this.getPeople().isEmpty()) this.disbandTeam(null, false);
            else this.disbandTeam(this.getPeople().get(RandomUtil.randomInt(this.getPeople().size())), false);
        }
        return this.leader;
    }

    public boolean isLeader(NexiaPlayer player) {
        if(player == null || player.unwrap() == null) return false;
        return player.equals(this.getLeader());
    }

    public List<NexiaPlayer> getPeople() {
        return this.people;
    }


    public boolean replaceLeader(@NotNull NexiaPlayer executor, @NotNull NexiaPlayer player, boolean message) {
        if(!this.isLeader(executor))  {
            if(message) executor.sendMessage(Component.text("You are not the leader!", ChatFormat.failColor));
            return false;
        }
        
        if(executor.equals(player) || this.isLeader(player)) {
            if(message) executor.sendMessage(Component.text("You are the leader!", ChatFormat.failColor));
            return false;
        }

        this.leader = player;
        this.getPeople().remove(player);
        this.getPeople().add(executor);

        if(message) executor.sendMessage(Component.text("You have promoted " + player.getRawName() + " to the leader.").color(ChatFormat.systemColor));
        for(NexiaPlayer tPlayer : this.all) {
            tPlayer.sendMessage(Component.text(player.getRawName() + " is now the party leader.").color(ChatFormat.systemColor));
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


    public void disbandTeam(NexiaPlayer executor, boolean message) {
        boolean isNull = executor == null || executor.unwrap() == null;

        if(!isNull && !this.isLeader(executor)) {
            executor.sendMessage(Component.text("You are not the team leader!", ChatFormat.failColor));
            return;
        }



        if(!this.getPeople().isEmpty()) {
            this.leader = this.getPeople().get(RandomUtil.randomInt(this.getPeople().size()));
            if(!isNull) this.leaveTeam(executor, true);
            return;
        }

        Component msg = Component.text("The duels team has been disbanded.", ChatFormat.failColor);

        for(NexiaPlayer player : all) {
            ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).duelOptions.duelsTeam = null;
            if(message) player.sendMessage(msg);
        }
        this.getPeople().clear();
        this.all.clear();
        this.alive.clear();
        this.leader = null;
        this.invited.clear();
        if(message && !isNull) executor.sendMessage(Component.text("You have disbanded your own team.").color(ChatFormat.systemColor));
    }

    public void leaveTeam(NexiaPlayer player, boolean message) {
        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(this.isLeader(player) && this.getPeople().isEmpty()) {
            this.disbandTeam(player, message);
            return;
        }
        if(this.isLeader(player) && !this.getPeople().isEmpty()) this.leader = this.getPeople().get(RandomUtil.randomInt(this.getPeople().size()));


        data.duelOptions.duelsTeam = null;
        this.getPeople().remove(player);
        this.all.remove(player);
        this.alive.remove(player);

        if(message) player.sendMessage(Component.text("You have left " + this.getLeader().getRawName() + "'s Team.", ChatFormat.normalColor));

        for(NexiaPlayer ap : this.all) {
            ap.sendMessage(Component.text(player.getRawName(), ChatFormat.brandColor1).append(Component.text(" has left the team.").color(ChatFormat.systemColor)));
        }
    }

    public void invitePlayer(NexiaPlayer invitor, NexiaPlayer player) {
        boolean isLeader = this.isLeader(invitor);

        if(!isLeader){
            invitor.sendMessage(Component.text("You are not the team leader!", ChatFormat.failColor));
            return;
        }

        if(player.equals(invitor)) {
            invitor.sendMessage(Component.text("You cannot invite yourself!", ChatFormat.failColor));
            return;
        }


        if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode != PlayerGameMode.LOBBY) {
            invitor.sendMessage(Component.text("That player is not in duels!", ChatFormat.failColor));
            return;
        }

        if(((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).duelOptions.duelsTeam != null) {
            invitor.sendMessage(Component.text("That player is already in a team!", ChatFormat.failColor));
            return;
        }

        if(this.getPeople().contains(player)) {
            invitor.sendMessage(Component.text("That player is already in your team!", ChatFormat.failColor));
            return;
        }

        for(NexiaPlayer ap : this.all) {
            ap.sendMessage(Component.text(player.getRawName(), ChatFormat.brandColor1).append(Component.text(" has been invited to the team.").color(ChatFormat.systemColor)));
        }

        this.invited.remove(player);
        this.invited.add(player);

        Component yes = Component.text("[", ChatFormat.Minecraft.dark_gray)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/party join " + invitor.getRawName())))
                .append(Component.text("]  ", ChatFormat.Minecraft.dark_gray)
                );

        Component no = Component.text("[", ChatFormat.Minecraft.dark_gray)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decoration(ChatFormat.bold, true)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me")
                                .color(ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/party decline " + invitor.getRawName())))
                .append(Component.text("]", ChatFormat.Minecraft.dark_gray)
                );

        player.sendMessage(
                Component.text(invitor.getRawName(), ChatFormat.brandColor2)
                        .append(Component.text(" has invited you to their team!", ChatFormat.normalColor))
        );

        player.sendMessage(yes.append(no));
    }

    public void listTeam(NexiaPlayer executor) {
        executor.sendMessage(Component.text("People on " + this.getLeader().getRawName() + "'s Team", ChatFormat.normalColor));
        for(NexiaPlayer player : this.all) {
            executor.sendMessage(Component.text("» ", ChatFormat.brandColor1).append(Component.text(player.getRawName(), ChatFormat.normalColor)));
        }
    }

    public void kickPlayer(NexiaPlayer executor, NexiaPlayer player) {
        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(!this.isLeader(executor)) {
            executor.sendMessage(Component.text("You are not the leader!", ChatFormat.failColor));
            return;
        }

        if(player.equals(executor)) {
            executor.sendMessage(Component.text("You cannot kick yourself!", ChatFormat.failColor));
            return;
        }

        if(!this.getPeople().contains(player)) {
            executor.sendMessage(Component.text("That player is not in your team!", ChatFormat.failColor));
            return;
        }


        for(NexiaPlayer ap : this.all) {
            ap.sendMessage(Component.text(player.getRawName(), ChatFormat.brandColor1).append(Component.text(" has been removed from the team.").color(ChatFormat.systemColor)));
        }

        data.duelOptions.duelsTeam = null;
        this.getPeople().remove(player);
        this.alive.remove(player);
        this.all.remove(player);

        player.sendMessage(Component.text("You have been kicked from " + executor.getRawName() + "'s Team.", ChatFormat.failColor));
    }

    public void joinTeam(NexiaPlayer player) {
        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(data.duelOptions.duelsTeam != null) {
            player.sendMessage(Component.text("You are currently in a team!", ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(player)) {
            this.getLeader().sendMessage(Component.text("That player did not invite you!", ChatFormat.failColor));
            return;
        }

        this.invited.remove(player);
        this.all.add(player);
        this.getPeople().add(player);

        data.duelOptions.duelsTeam = this;
        player.sendMessage(Component.text("You have joined " + this.getLeader().getRawName() + "'s team", ChatFormat.normalColor));
        for(NexiaPlayer ap : this.all) {
            ap.sendMessage(Component.text(player.getRawName(), ChatFormat.brandColor1).append(Component.text(" has joined the team.")));
        }
    }

    public void declineTeam(NexiaPlayer player) {
        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(data.duelOptions.duelsTeam != null) {
            player.sendMessage(Component.text("You are currently in a team!", ChatFormat.failColor));
            return;
        }

        if(!this.invited.contains(player)) {
            this.getLeader().sendMessage(Component.text("That player did not invite you!", ChatFormat.failColor));
            return;
        }

        this.invited.remove(player);
        player.sendMessage(Component.text("You have declined " + this.getLeader().getRawName() + "'s invite.", ChatFormat.normalColor));
        this.getLeader().sendMessage(Component.text(player.getRawName() + " has declined your invite.", ChatFormat.failColor));
    }

    public static DuelsTeam createTeam(NexiaPlayer player, boolean message) {
        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(data.duelOptions.duelsTeam != null) {
            if(message) {
                player.sendNexiaMessage("You are currently in a team!");
            }
            return null;
        }

        if(message) {
            player.sendNexiaMessage("You have created a team.");
        }

        DuelsTeam team = new DuelsTeam(player, new ArrayList<>());
        data.duelOptions.duelsTeam = team;

        return team;
    }
}
