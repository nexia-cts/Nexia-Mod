package com.nexia.minigames.games.football;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.util.player.PlayerData;
import com.nexia.minigames.games.football.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.notcoded.codelib.util.TickUtil;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Predicate;

public class FootballGame {
    public static ArrayList<AccuratePlayer> players = new ArrayList<>();

    public static ArrayList<AccuratePlayer> spectator = new ArrayList<>();

    public static ServerLevel world = null;

    public static FootballMap map = FootballMap.STADIUM;

    public static FootballTeam team1 = new FootballTeam(new ArrayList<>(), map.team1Pos);
    public static FootballTeam team2 = new FootballTeam(new ArrayList<>(), map.team2Pos);


    // Both timers counted in seconds.
    public static int gameTime = 600;

    public static int queueTime = 15;

    public static ArrayList<AccuratePlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;

    private static FootballTeam winnerTeam = null;

    public static final String FOOTBALL_TAG = "football";

    private static int endTime = 5;


    public static void leave(ServerPlayer minecraftPlayer) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        AccuratePlayer accuratePlayer = AccuratePlayer.create(minecraftPlayer);

        PlayerData data = PlayerDataManager.get(minecraftPlayer);
        FootballGame.spectator.remove(accuratePlayer);
        FootballGame.queue.remove(accuratePlayer);
        FootballGame.players.remove(accuratePlayer);

        if(data.gameMode.equals(FootballGameMode.PLAYING) && (winnerTeam == null || !winnerTeam.players.contains(accuratePlayer))) data.savedData.loss++;
        data.team = null;

        player.removeTag("in_football_game");

        player.reset(true, Minecraft.GameMode.ADVENTURE);

        if(!FootballGame.team1.refreshTeam()) FootballGame.endGame(FootballGame.team2);
        if(!FootballGame.team2.refreshTeam()) FootballGame.endGame(FootballGame.team1);

        player.removeTag("football");

        data.gameMode = FootballGameMode.LOBBY;
    }

    public static void second() {
        if(FootballGame.isStarted) {
            if(FootballGame.isEnding) {
                int color = 244 * 65536 + 166 * 256 + 71;
                // r * 65536 + g * 256 + b;

                if(winnerTeam != null) {
                    ServerPlayer randomPlayer = winnerTeam.players.get(RandomUtil.randomInt(winnerTeam.players.size())).get();
                    if(randomPlayer != null) DuelGameHandler.winnerRockets(randomPlayer, FootballGame.world, color);
                }

                if(FootballGame.endTime <= 0) {
                    for(NexiaPlayer player : FootballGame.getViewers()){
                        player.runCommand("/hub", 0, false);
                    }

                    FootballGame.resetAll();
                }

                FootballGame.endTime--;
            } else {
                FootballGame.updateInfo();

                if(FootballGame.gameTime <= 0 && !FootballGame.isEnding){

                    int team1 = FootballGame.team1.goals;
                    int team2 = FootballGame.team2.goals;


                    if(team1 == team2) endGame(null);
                    if(team1 > team2) endGame(FootballGame.team1);
                    else endGame(FootballGame.team2);
                } else if(FootballGame.gameTime > 0 && !FootballGame.isEnding){
                    FootballGame.gameTime--;
                }
            }


        } else {
            if(FootballGame.queue.size() >= 2) {
                for(NexiaPlayer player : FootballGame.queue){
                    if(player == null || player.unwrap() == null) return;

                    if(FootballGame.queueTime <= 5) {
                        Title title = getTitle(FootballGame.queueTime);

                        player.sendTitle(title);
                        player.sendSound(new EntityPos(player.unwrap()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
                    }

                    fPlayer.sendActionBarMessage(
                            Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                                    .append(Component.text(FootballGame.map.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                    .append(Component.text(" (" + FootballGame.queue.size() + ")").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(FootballGame.queueTime).color(ChatFormat.brandColor2))
                    );

                    if(FootballGame.queueTime <= 5 || FootballGame.queueTime == 10 || FootballGame.queueTime == 15) {
                        fPlayer.sendMessage(Component.text("The game will start in ").color(TextColor.fromHexString("#b3b3b3"))
                                .append(Component.text(FootballGame.queueTime).color(ChatFormat.brandColor1))
                                .append(Component.text(" seconds.").color(TextColor.fromHexString("#b3b3b3")))
                        );
                    }
                }

                FootballGame.queueTime--;
            } else {
                FootballGame.queueTime = 15;
            }
            if(FootballGame.queueTime <= 0) startGame();
        }
    }

    public static void goal(ArmorStand entity, FootballTeam team) {

        if(!team1.refreshTeam() || !team2.refreshTeam()) endGame(null);

        if(!FootballGame.isEnding) {
            ServerPlayer closestPlayer = (ServerPlayer) FootballGame.world.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 20, e -> e instanceof ServerPlayer se && !se.isSpectator() && !se.isCreative() && team.players.contains(new NexiaPlayer(se)));
            if(closestPlayer != null) PlayerDataManager.get(closestPlayer.getUUID()).savedData.goals++;
            team.goals++;
            if(team.goals >= FootballGame.map.maxGoals) FootballGame.endGame(team);
        }

        FootballGame.updateInfo();

        //PlayerDataManager.get(scorer.get()).savedData.goals++;

        int teamID = 1;
        if(team == FootballGame.team2) teamID = 2;

        if(!FootballGame.isEnding) {
            entity.setDeltaMovement(0, 0, 0);
            entity.moveTo(0, 80, 0, 0, 0);

            for(ServerPlayer player : FootballGame.getViewers()) {
                PlayerUtil.getFactoryPlayer(player).sendTitle(Title.title(Component.text("Team " + teamID).color(ChatFormat.brandColor2), Component.text("has scored a goal!").color(ChatFormat.normalColor)));
            }

            for(NexiaPlayer player : FootballGame.team1.players) {
                FootballGame.team1.spawnPosition.teleportPlayer(FootballGame.world, player.unwrap());
            }

            for(NexiaPlayer player : FootballGame.team2.players) {
                FootballGame.team2.spawnPosition.teleportPlayer(FootballGame.world, player.unwrap());
            }
        }



    }

    @NotNull
    private static Title getTitle(int queueTime) {
        TextColor color = NamedTextColor.GREEN;

        if (queueTime <= 3 && queueTime > 1) {
            color = NamedTextColor.YELLOW;
        } else if (queueTime <= 1) {
            color = NamedTextColor.RED;
        }

        return Title.title(Component.text(queueTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
    }

    public static void joinQueue(ServerPlayer player) {
        com.nexia.minigames.games.football.util.player.PlayerData data = PlayerDataManager.get(player);
        data.team = null;
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        accuratePlayer.get().setHealth(accuratePlayer.get().getMaxHealth());
        if(FootballGame.isStarted){
            FootballGame.spectator.add(accuratePlayer);
            PlayerDataManager.get(player).gameMode = FootballGameMode.SPECTATOR;
            player.setGameMode(Minecraft.GameMode.SPECTATOR);
        } else {
            FootballGame.queue.add(player);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }

        player.unwrap().teleportTo(world, 0, 101, 0, 0, 0);
        player.unwrap().setRespawnPosition(world.dimension(), new BlockPos(0, 100, 0), 0, true, false);
    }

    public static void endGame(FootballTeam winnerTeam) {
        FootballGame.isEnding = true;

        if(winnerTeam == null) {
            Component msg = Component.text("The game was a ")
                    .color(ChatFormat.normalColor)
                    .append(Component.text("draw").color(ChatFormat.brandColor2))
                    .append(Component.text("!").color(ChatFormat.normalColor)
                    );
            for(NexiaPlayer player : FootballGame.getViewers()){
                if(player != null && player.unwrap() != null) player.sendTitle(Title.title(msg, Component.text("")));
            }

            return;
        }

        FootballGame.winnerTeam = winnerTeam;

        int teamID = 1;
        if(winnerTeam == FootballGame.team2) teamID = 2;

        for(AccuratePlayer accuratePlayer : winnerTeam.players) {
            PlayerUtil.getFactoryPlayer(accuratePlayer.get()).sendTitle(Title.title(Component.text("You won!").color(ChatFormat.greenColor), Component.text("")));
            PlayerDataManager.get(accuratePlayer.get()).savedData.wins++;
        }

        for(ServerPlayer player : FootballGame.getViewers()){
            PlayerUtil.getFactoryPlayer(player).sendTitle(Title.title(Component.text("Team " + teamID).color(ChatFormat.brandColor2), Component.text("has won the game! (" + winnerTeam.goals + " goals)").color(ChatFormat.normalColor)));
        }
    }

    public static void updateInfo() {

        String[] timer = TickUtil.minuteTimeStamp(FootballGame.gameTime * 20);
        for(ServerPlayer player : FootballGame.getViewers()) {
            if(player == null) return;
            FootballTeam playerTeam = PlayerDataManager.get(player).team;
            if(playerTeam == null) playerTeam = FootballGame.team1; // maybe cuz spectator
            FootballTeam otherTeam = FootballGame.team1;
            if(playerTeam.equals(FootballGame.team1)) otherTeam = FootballGame.team2;

            PlayerUtil.getFactoryPlayer(player).sendActionBarMessage(
                    Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                            .append(Component.text(FootballGame.map.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(timer[0] + ":" + timer[1]).color(ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Goals » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(playerTeam.goals + "/" + FootballGame.map.maxGoals).color(ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Enemy Team Goals » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(otherTeam.goals + "/" + FootballGame.map.maxGoals).color(ChatFormat.brandColor2))
            );
        }
    }

    private static ArmorStand createArmorStand() {
        ArmorStand armorStand = new ArmorStand(FootballGame.world, 0, 81, 0);
        armorStand.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 5, true, true));
        DyeableLeatherItem leatherItem = (DyeableLeatherItem) Items.LEATHER_CHESTPLATE;

        ItemStack helmet = Items.LEATHER_HELMET.getDefaultInstance();
        helmet.getOrCreateTag().putInt("Unbreakable", 1);

        ItemStack chestplate = Items.LEATHER_CHESTPLATE.getDefaultInstance();
        chestplate.getOrCreateTag().putInt("Unbreakable", 1);

        ItemStack leggings = Items.LEATHER_LEGGINGS.getDefaultInstance();
        leggings.getOrCreateTag().putInt("Unbreakable", 1);

        ItemStack boots = Items.LEATHER_BOOTS.getDefaultInstance();
        boots.getOrCreateTag().putInt("Unbreakable", 1);

        // r * 65536 + g * 256 + b
        int black = 0;
        int white = 255 * 65536 + 255 * 256 + 255;

        leatherItem.setColor(helmet, white);
        leatherItem.setColor(chestplate, black);
        leatherItem.setColor(leggings, white);
        leatherItem.setColor(boots, black);

        armorStand.setItemSlot(EquipmentSlot.HEAD, helmet);
        armorStand.setItemSlot(EquipmentSlot.CHEST, chestplate);
        armorStand.setItemSlot(EquipmentSlot.LEGS, leggings);
        armorStand.setItemSlot(EquipmentSlot.FEET, boots);

        //armorStand.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0.0);

        armorStand.setNoBasePlate(true);
        //armorStand.setInvisible(true);

        FootballGame.world.addFreshEntity(armorStand);

        return armorStand;
    }

    private static FootballTeam assignPlayer(AccuratePlayer player) {
        int players = FootballGame.players.size();
        int random = RandomUtil.randomInt(1, 2);

        int team1 = FootballGame.team1.players.size();
        int team2 = FootballGame.team2.players.size();

        if(team1 >= players/2) {
            FootballGame.team2.addPlayer(player);
            return FootballGame.team2;
        } else if (team2 >= players/2) {
            FootballGame.team1.addPlayer(player);
            return FootballGame.team1;
        }

        if(random == 1) {
            FootballGame.team1.addPlayer(player);
            return FootballGame.team1;
        } else if (random == 2) {
            FootballGame.team2.addPlayer(player);
            return FootballGame.team2;
        }

        return null;
    }

    public static void startGame() {
        if(FootballGame.queueTime <= 0){
            FootballGame.isStarted = true;
            FootballGame.gameTime = 600;
            FootballGame.players.addAll(FootballGame.queue);

            // leather armor dyed blue/red depending on the team

            ItemStack kicking = new ItemStack(Items.NETHERITE_SWORD);
            kicking.getOrCreateTag().putBoolean("Unbreakable", true);
            kicking.enchant(Enchantments.KNOCKBACK, 4);
            kicking.setHoverName(new TextComponent("§7§lKicking §7Sword §8[§710s cooldown§8]"));
            kicking.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            /*
            ItemStack normal = new ItemStack(Items.IRON_SWORD);
            normal.getOrCreateTag().putBoolean("Unbreakable", true);
            normal.enchant(Enchantments.KNOCKBACK, 2);
            normal.setHoverName(new TextComponent("§f§lNormal §fSword §8[§fno cooldown§8]"));
            normal.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);


             */
            FootballGame.createArmorStand();

            for(NexiaPlayer player : FootballGame.players) {
                //NexiaPlayer.inventory.setItem(0, normal);
                //NexiaPlayer.inventory.setItem(1, kicking);
                player.unwrap().inventory.setItem(0, kicking);

                PlayerData data = PlayerDataManager.get(serverPlayer);
                data.gameMode = FootballGameMode.PLAYING;

                player.addTag("in_football_game");
                player.addTag(LobbyUtil.NO_DAMAGE_TAG);
                player.unwrap().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 255, false, false, false));

                data.team = FootballGame.assignPlayer(player);
                while(data.team == null) {
                    data.team = FootballGame.assignPlayer(player);
                    // if you're still null then im going to beat the shit out of you
                }
                data.team.spawnPosition.teleportPlayer(FootballGame.world, player.unwrap());

                ItemStack helmet = Items.LEATHER_HELMET.getDefaultInstance();
                helmet.getOrCreateTag().putInt("Unbreakable", 1);

                ItemStack chestplate = Items.LEATHER_CHESTPLATE.getDefaultInstance();
                chestplate.getOrCreateTag().putInt("Unbreakable", 1);

                ItemStack leggings = Items.LEATHER_LEGGINGS.getDefaultInstance();
                leggings.getOrCreateTag().putInt("Unbreakable", 1);

                ItemStack boots = Items.LEATHER_BOOTS.getDefaultInstance();
                boots.getOrCreateTag().putInt("Unbreakable", 1);

                int colour = 0;

                if(data.team.equals(FootballGame.team1)) {
                    // r * 65536 + g * 256 + b
                    colour = 255 * 65536;
                } else if(data.team.equals(FootballGame.team2)) {
                    colour = 255;
                }

                DyeableLeatherItem leatherItem = (DyeableLeatherItem) Items.LEATHER_HELMET;

                leatherItem.setColor(helmet, colour);
                leatherItem.setColor(chestplate, colour);
                leatherItem.setColor(leggings, colour);
                leatherItem.setColor(boots, colour);

                player.unwrap().setItemSlot(EquipmentSlot.HEAD, helmet);
                player.unwrap().setItemSlot(EquipmentSlot.CHEST, chestplate);
                player.unwrap().setItemSlot(EquipmentSlot.LEGS, leggings);
                player.unwrap().setItemSlot(EquipmentSlot.FEET, boots);


                player.setGameMode(Minecraft.GameMode.SURVIVAL);
                //player.setRespawnPosition(world.dimension(), pos, 0, true, false);
                player.unwrap().getCooldowns().addCooldown(Items.NETHERITE_SWORD, 200);
            }

            FootballGame.spectator.clear();
            FootballGame.queue.clear();
        }
    }

    public static boolean isFootballPlayer(NexiaPlayer player){
        return com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.FOOTBALL || player.hasTag("football") || player.hasTag("in_football_game");
    }

    public static void resetAll() {
        queue.clear();
        players.clear();
        spectator.clear();

        map = FootballMap.footballMaps.get(RandomUtil.randomInt(FootballMap.footballMaps.size()));
        world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("football", FootballGame.map.id), new RuntimeWorldConfig()).asWorld();

        isStarted = false;
        queueTime = 15;
        gameTime = 600;
        isEnding = false;
        team1 = new FootballTeam(new ArrayList<>(), map.team1Pos);
        team2 = new FootballTeam(new ArrayList<>(), map.team2Pos);
        winnerTeam = null;
        endTime = 5;
    }

    public static void tick() {

        AABB aabb = new AABB(FootballGame.map.corner1, FootballGame.map.corner2);
        Predicate<Entity> predicate = o -> true;

        for (ItemEntity entity : FootballGame.world.getEntities(EntityType.ITEM, aabb, predicate)) {
            // kill @e[type=item,distance=0..]
            entity.remove();
        }

        if(!FootballGame.isStarted) {
            for (ArmorStand entity : FootballGame.world.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
                // kill @e[type=item,distance=0..]
                entity.remove();
            }
            return;
        }

        // check if armor stand (football) is in goal, then reset football to middle and give goal

        aabb = new AABB(FootballGame.map.team1goalCorner1, FootballGame.map.team1goalCorner2);
        for (ArmorStand entity : FootballGame.world.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            FootballGame.goal(entity, FootballGame.team2);
        }
        aabb = new AABB(FootballGame.map.team2goalCorner1, FootballGame.map.team2goalCorner2);
        for (ArmorStand entity : FootballGame.world.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            FootballGame.goal(entity, FootballGame.team1);
        }
    }

    public static void firstTick(){
        resetAll();
    }

    public static ArrayList<ServerPlayer> getViewers() {
        ArrayList<ServerPlayer> viewers = new ArrayList<>();
        FootballGame.players.forEach(player -> viewers.add(player.get()));
        FootballGame.spectator.forEach(player -> viewers.add(player.get()));
        return viewers;
    }
}
