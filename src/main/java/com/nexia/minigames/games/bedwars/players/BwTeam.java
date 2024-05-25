package com.nexia.minigames.games.bedwars.players;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.upgrades.BwTrap;
import com.nexia.minigames.games.bedwars.upgrades.BwUpgrade;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Predicate;

public class BwTeam {

    public String displayName;
    public String color;
    public String textColor;
    public String textColorName;
    public int armorColor;

    public PlayerTeam scoreboardTeam;
    public BlockPos bedLocation = null;
    public EntityPos spawn = null;
    public EntityPos genLocation = null;
    public ArrayList<NexiaPlayer> players = new ArrayList<>();

    public HashMap<String, BwUpgrade> upgrades = BwUpgrade.newUpgradeSet();
    public HashMap<String, BwTrap> traps = BwTrap.newTrapSet();

    private BwTeam(String displayName, String colorName, String colorId, String textColorName, int r, int g, int b) {
        this.displayName = displayName;
        this.color = colorName;
        this.textColor = "\247" + colorId;
        this.textColorName = textColorName;
        this.armorColor = r * 65536 + g * 256 + b;
        this.scoreboardTeam = getTeam();
    }

    private PlayerTeam getTeam() {
        ServerScoreboard scoreboard = ServerTime.minecraftServer.getScoreboard();
        String teamName = "bw_" + color;
        PlayerTeam playerTeam = scoreboard.getPlayerTeam(teamName);

        if (scoreboard.getPlayerTeam(teamName) == null) {
            playerTeam = scoreboard.addPlayerTeam(teamName);
        }
        if (teamName.length() > 16) {
            teamName = teamName.substring(16);
        }

        playerTeam.setDisplayName(new TextComponent(teamName));
        playerTeam.setPlayerPrefix(new TextComponent(textColor  + "\247lBW " ));

        playerTeam.setDeathMessageVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);

        return playerTeam;
    }

    // Team list --------------------------------------------------------------------------

    public static HashMap<String, BwTeam> allTeams;
    public static ArrayList<BwTeam> teamsInOrder;

    public static void resetTeams() {
        allTeams = new HashMap<>();
        teamsInOrder = new ArrayList<>();

        addTeam(new BwTeam("Red", "red", "c", "red", 255, 0, 0));
        addTeam(new BwTeam("Orange", "orange", "6", "gold", 255, 130, 30));
        addTeam(new BwTeam("Yellow", "yellow", "e", "yellow", 255, 255, 0));
        addTeam(new BwTeam("Lime", "lime", "a", "green", 20, 255, 25));
        addTeam(new BwTeam("Green", "green", "2", "dark_green", 0, 120, 0));
        addTeam(new BwTeam("Aqua", "light_blue", "b", "aqua", 10, 240, 255));
        addTeam(new BwTeam("Blue", "blue", "9", "blue", 0, 50, 255));
        addTeam(new BwTeam("Purple", "purple", "d", "light_purple", 150, 0, 150));

        BwGame.maxPlayerCount = BwGame.playersInTeam * allTeams.size();
    }

    private static void addTeam(BwTeam team) {
        allTeams.put(team.color, team);
        teamsInOrder.add(team);
    }

    // PLayer util --------------------------------------------------------------------------

    public static ArrayList<BwTeam> getAliveTeams() {
        ArrayList<BwTeam> aliveTeams = new ArrayList<>();
        for (BwTeam team : allTeams.values()) {
            if (!team.players.isEmpty()) {
                aliveTeams.add(team);
            }
        }
        return aliveTeams;
    }

    public static void spreadIntoTeams(ArrayList<NexiaPlayer> queueList) {
        ServerScoreboard scoreboard = ServerTime.minecraftServer.getScoreboard();
        Random random = BwAreas.bedWarsWorld.getRandom();

        ArrayList<BwTeam> availableTeams = new ArrayList<>(teamsInOrder);

        while (!queueList.isEmpty() && !availableTeams.isEmpty()) {
            NexiaPlayer player = queueList.getFirst();

            BwTeam team = availableTeams.get(random.nextInt(availableTeams.size()));
            team.players.add(player);
            availableTeams.remove(team);

            player.getFactoryPlayer().addTag(LobbyUtil.NO_RANK_DISPLAY_TAG);
            scoreboard.addPlayerToTeam(player.player().name, team.scoreboardTeam);


            queueList.remove(player);
        }

        queueList.clear();
    }

    public static BwTeam getPlayerTeam(NexiaPlayer player) {
        for (BwTeam team : allTeams.values()) {
            if (team.players.contains(player)) {
                return team;
            }
        }
        return null;
    }

    public static boolean fixTeamPlayer(NexiaPlayer player) {
        for (BwTeam team : allTeams.values()) {
            for (int i = 0; i < team.players.size(); i++) {
                if (team.players.get(i).player().uuid.equals(player.player().uuid)) {
                    team.players.set(i, player);
                    return true;
                }
            }
        }
        return false;
    }

    public static String getPlayerTeamColor(NexiaPlayer player) {
        BwTeam team = getPlayerTeam(player);
        if (team == null) return null;

        return team.color;
    }

    // Spawn util --------------------------------------------------------------------------

    public static final String spawnTagStart = "spawnLocation_";
    public static final String bedTagStart = "bedLocation_";

    public static void setSpawns() {
        AABB aabb = new AABB(BwAreas.bedWarsCorner1, BwAreas.bedWarsCorner2);
        Predicate<Entity> predicate = o -> true;

        for (Entity entity : BwAreas.bedWarsWorld.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            for (String tag : entity.getTags()) {
                if (tag.startsWith(spawnTagStart)) {
                    tag = tag.replaceFirst(spawnTagStart, "");
                    if (!allTeams.containsKey(tag)) return;
                    allTeams.get(tag).spawn = new EntityPos(entity);
                }
            }
        }
    }

    public static void createBeds() {
        AABB aabb = new AABB(BwAreas.bedWarsCorner1, BwAreas.bedWarsCorner2);
        Predicate<Entity> predicate = o -> true;

        for (Entity entity : BwAreas.bedWarsWorld.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            for (String tag : entity.getTags()) {
                if (tag.startsWith(bedTagStart)) {
                    addPossibleBed(entity, tag);
                }
            }
        }
    }

    private static void addPossibleBed(Entity entity, String tag) {
        tag = tag.replaceFirst(bedTagStart, "");
        if (!allTeams.containsKey(tag)) return;

        BwTeam team = allTeams.get(tag);
        BlockPos blockPos = new BlockPos(entity.position());

        if (team.players.isEmpty()) {
            BwAreas.bedWarsWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        Block block = Registry.BLOCK.get(new ResourceLocation(team.color + "_bed"));
        if (!(block instanceof BedBlock)) return;
        BlockUtil.placeBed(BwAreas.bedWarsWorld, (BedBlock)block, blockPos, entity.yRot);
        team.bedLocation = blockPos;
    }

    public static boolean bedExists(BwTeam team) {
        if (team.bedLocation == null) return false;

        BlockState blockState = BwAreas.bedWarsWorld.getBlockState(team.bedLocation);
        return BlockUtil.blockToText(blockState).equals(team.color + "_bed");
    }

    // ----- UTILITIES -------------------------------------------------------------------------------

    public static void reloadPlayerTeamColors() {
        for (BwTeam team : teamsInOrder) {
            ServerTime.factoryServer.runCommand(String.format("team modify %s color %s", team.scoreboardTeam.getName(), team.textColorName), 4, false);
        }
    }

    public static void winnerRockets(ArrayList<NexiaPlayer> winners, Integer winnerColor) {
        if (winners.isEmpty() || winnerColor == null) return;

        ArrayList<EntityPos> positions = new ArrayList<>();
        for (NexiaPlayer player : winners) {
            Random random = BwAreas.bedWarsWorld.getRandom();
            positions.add(new EntityPos(player.player().get()).add(random.nextInt(9) - 4, 2, random.nextInt(9) - 4));
        }
        positions.add(BwAreas.bedWarsCenter.c().add(0.5, 2, 0.5));

        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
        try {
            itemStack.setTag(TagParser.parseTag("{Fireworks:{Explosions:[{Type:0,Flicker:1b,Trail:1b,Colors:[I;" +
                    winnerColor + "]}]}}"));
        } catch (Exception ignored) {}

        for (EntityPos pos : positions) {
            FireworkRocketEntity rocket = new FireworkRocketEntity(BwAreas.bedWarsWorld, pos.x, pos.y, pos.z, itemStack);
            BwAreas.bedWarsWorld.addFreshEntity(rocket);
        }
    }

    public void announceBedBreak(NexiaPlayer breaker, BlockPos blockPos) {
        String breakerColor = "";
        BwTeam breakerTeam = getPlayerTeam(breaker);
        if (breakerTeam != null) breakerColor = breakerTeam.textColor;

        for(NexiaPlayer viewer : BwPlayers.getViewers()) {
            viewer.sendMessage(Component.text(String.format("%s%s bed", textColor, displayName))
                    .append(Component.text(" has been destroyed by").color(ChatFormat.brandColor2))
                    .append(Component.text(breakerColor + breaker.player().name))
            );
        }

        for(NexiaPlayer player : players) {
            player.sendTitle(Title.title(Component.text("Bed Destroyed").color(ChatFormat.failColor), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0))));
        }

        BwScoreboard.updateScoreboard();

        for (NexiaPlayer player : BwPlayers.getViewers()) {
            float volume;
            if (players.contains(player)) {
                volume = 0.04f;
            } else {
                float distance = (float) new EntityPos(player.player().get()).distance(new EntityPos(blockPos));
                distance = Math.min(distance, 20f);
                volume = 0.03f - (distance * 0.001f);
            }
            player.sendSound(SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.MASTER, volume, 1f);
        }
    }

}
