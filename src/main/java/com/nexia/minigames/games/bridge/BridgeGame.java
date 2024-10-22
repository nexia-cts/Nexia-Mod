package com.nexia.minigames.games.Bridge;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.time.TickUtil;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.Bridge.util.player.BridgePlayerData;
import com.nexia.nexus.api.world.types.Minecraft;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Predicate;

import static com.nexia.core.NexiaCore.BRIDGE_DATA_MANAGER;

public class BridgeGame {
    public static ArrayList<NexiaPlayer> players = new ArrayList<>();

    public static ArrayList<NexiaPlayer> spectator = new ArrayList<>();

    public static ServerLevel world = null;

    public static BridgeMap map = BridgeMap.STADIUM;

    public static BridgeTeam team1 = new BridgeTeam(new ArrayList<>(), map.team1Pos);
    public static BridgeTeam team2 = new BridgeTeam(new ArrayList<>(), map.team2Pos);


    // Both timers counted in seconds.
    public static int gameTime = 600;

    public static int queueTime = 15;

    public static ArrayList<NexiaPlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;

    private static BridgeTeam winnerTeam = null;

    public static final String BRIDGE_TAG = "bridge";

    private static int endTime = 5;


    public static void leave(NexiaPlayer player) {
        Bridge PlayerData data = (BridgePlayerData) PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(player);
        BridgeGame.spectator.remove(player);
        BridgeGame.queue.remove(player);
        BridgeGame.players.remove(player);

        if(data.gameMode.equals(BridgeGameMode.PLAYING) && (winnerTeam == null || !winnerTeam.players.contains(player))) data.savedData.incrementInteger("losses");
        data.team = null;

        player.removeTag("in_bridge_game");

        player.reset(true, Minecraft.GameMode.ADVENTURE);

        if(!BridgeGame.team1.refreshTeam()) BridgeGame.endGame(BridgeGame.team2);
        if(!BridgeGame.team2.refreshTeam()) BridgeGame.endGame(BridgeGame.team1);

        player.removeTag("bridge");

        data.gameMode = BridgeGameMode.LOBBY;
    }

    public static void death(NexiaPlayer victim, @Nullable PlayerDeathEvent playerDeathEvent){
        BridgePlayerData victimData = (BridgePlayerData) PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(victim);

        if(playerDeathEvent != null) {
            playerDeathEvent.setDropEquipment(false);
            playerDeathEvent.setDropExperience(false);
            playerDeathEvent.setDropLoot(false);
            data.team.spawnPosition.teleportPlayer(BridgeGame.world, player.unwrap());
            giveKit(victim);
        }

    }

    public static void giveKit(NexiaPlayer player) {
        player.getInventory().clear();

        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.getOrCreateTag().putBoolean("Unbreakable", true);
        sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        ItemStack bow = new ItemStack(Items.BOW);
        ItemDisplayUtil.addGlint(bow);
        bow.getOrCreateTag().putBoolean("Unbreakable", true);
        bow.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        player.unwrap().getCooldowns().addCooldown(Items.BOW, 3)

        ItemStack chestplate = Items.LEATHER_CHESTPLATE.getDefaultInstance();
        chestplate.getOrCreateTag().putInt("Unbreakable", 1);

        ItemStack leggings = Items.LEATHER_LEGGINGS.getDefaultInstance();
        leggings.getOrCreateTag().putInt("Unbreakable", 1);

        ItemStack boots = Items.LEATHER_BOOTS.getDefaultInstance();
        boots.getOrCreateTag().putInt("Unbreakable", 1);

        int colour = 0;

        ItemStack blocks = new ItemStack(Items.GRAY_STAINED_CLAY);

        if(data.team.equals(BridgeGame.team1)) {
            // r * 65536 + g * 256 + b
            colour = 255 * 65536;
            blocks = new ItemStack(Items.BLUE_STAINED_CLAY);

        } else if(data.team.equals(BridgeGame.team2)) {
            colour = 255;
            blocks = new ItemStack(Items.RED_STAINED_CLAY);
        }

        DyeableLeatherItem leatherItem = (DyeableLeatherItem) Items.LEATHER_CHESTPLATE;

//                leatherItem.setColor(helmet, colour);
        leatherItem.setColor(chestplate, colour);
        leatherItem.setColor(leggings, colour);
        leatherItem.setColor(boots, colour);

//                player.unwrap().setItemSlot(EquipmentSlot.HEAD, helmet);
        player.unwrap().setItemSlot(EquipmentSlot.CHEST, chestplate);
        player.unwrap().setItemSlot(EquipmentSlot.LEGS, leggings);
        player.unwrap().setItemSlot(EquipmentSlot.FEET, boots);


        ItemStack pickaxe = Items.DIAMOND_PICKAXE.getDefaultInstance();
        pickaxe.getOrCreateTag().putInt("Efficency", 4);

        ItemStack arrow = new ItemStack(Items.ARROW);

        ItemStack gap = Items.GOLDEN_APPLE.getDefaultInstance();

        player.unwrap().setSlot(0, sword);
        player.unwrap().setSlot(1, bow);
        player.unwrap().setSlot(2, arrow);
        player.unwrap().setSlot(4, blocks);
        player.unwrap().setSlot(5, blocks);
        player.unwrap().setSlot(6, pickaxe);
        player.unwrap().setSlot(9, gap);

    }

    public static void second() {
        if(BridgeGame.isStarted) {
            if(BridgeGame.isEnding) {
                int color = 244 * 65536 + 166 * 256 + 71;
                // r * 65536 + g * 256 + b;

                if(winnerTeam != null) {
                    NexiaPlayer randomPlayer = winnerTeam.players.get(RandomUtil.randomInt(winnerTeam.players.size()));
                    if(randomPlayer != null) DuelGameHandler.winnerRockets(randomPlayer, BridgeGame.world, color);
                }

                if(BridgeGame.endTime <= 0) {
                    for(NexiaPlayer player : BridgeGame.getViewers()){
                        player.runCommand("/hub", 0, false);
                    }

                    BridgeGame.resetAll();
                }

                BridgeGame.endTime--;
            } else {
                BridgeGame.updateInfo();

                if(BridgeGame.gameTime <= 0 && !BridgeGame.isEnding){

                    int team1 = BridgeGame.team1.goals;
                    int team2 = BridgeGame.team2.goals;


                    if(team1 == team2) endGame(null);
                    if(team1 > team2) endGame(BridgeGame.team1);
                    else endGame(BridgeGame.team2);
                } else if(BridgeGame.gameTime > 0 && !BridgeGame.isEnding){
                    BridgeGame.gameTime--;
                }
            }


        } else {
            if(BridgeGame.queue.size() >= 2) {
                for(NexiaPlayer player : BridgeGame.queue){
                    if(player == null || player.unwrap() == null) return;

                    if(BridgeGame.queueTime <= 5) {
                        Title title = getTitle(BridgeGame.queueTime);

                        player.sendTitle(title);
                        player.sendSound(new EntityPos(player.unwrap()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
                    }

                    player.sendActionBarMessage(
                            Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                                    .append(Component.text(BridgeGame.map.name, ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                    .append(Component.text(" (" + BridgeGame.queue.size() + ")").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(BridgeGame.queueTime, ChatFormat.brandColor2))
                    );

                    if(BridgeGame.queueTime <= 5 || BridgeGame.queueTime == 10 || BridgeGame.queueTime == 15) {
                        player.sendMessage(Component.text("The game will start in ").color(TextColor.fromHexString("#b3b3b3"))
                                .append(Component.text(BridgeGame.queueTime, ChatFormat.brandColor1))
                                .append(Component.text(" seconds.").color(TextColor.fromHexString("#b3b3b3")))
                        );
                    }
                }

                BridgeGame.queueTime--;
            } else {
                BridgeGame.queueTime = 15;
            }
            if(BridgeGame.queueTime <= 0) startGame();
        }
    }

    public static void goal(BridgeTeam team) {
//        endPortalblock.java isInBlock


        if(!team1.refreshTeam() || !team2.refreshTeam()) endGame(null);

        if(!BridgeGame.isEnding) {
            ServerPlayer closestPlayer = (ServerPlayer) BridgeGame.world.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 20, e -> e instanceof ServerPlayer se && !se.isSpectator() && !se.isCreative() && team.players.contains(new NexiaPlayer(se)));
            if(closestPlayer != null) PlayerDataManager.getDataManager(Bridge_DATA_MANAGER).get(closestPlayer.getUUID()).savedData.incrementInteger("goals");
            team.goals++;
            if(team.goals >= BridgeGame.map.maxGoals) BridgeGame.endGame(team);
        }

        BridgeGame.updateInfo();

        //PlayerDataManager.getDataManager(NexiaCore.FOOTBALL_DATA_MANAGER).get(scorer.get()).savedData.goals++;

        int teamID = 1;
        if(team == BridgeGame.team2) teamID = 2;

        if(!BridgeGame.isEnding) {
//            entity.setDeltaMovement(0, 0, 0);
//            entity.moveTo(0, 80, 0, 0, 0);

            for(NexiaPlayer player : BridgeGame.getViewers()) {
                player.sendTitle(Title.title(Component.text("Team " + teamID, ChatFormat.brandColor2), Component.text("has scored a goal!", ChatFormat.normalColor)));
            }

            for(NexiaPlayer player : BridgeGame.team1.players) {
                BridgeGame.team1.spawnPosition.teleportPlayer(BridgeGame.world, player.unwrap());
            }

            for(NexiaPlayer player : BridgeGame.team2.players) {
                BridgeGame.team2.spawnPosition.teleportPlayer(BridgeGame.world, player.unwrap());
            }
        }



    }

    @NotNull
    private static Title getTitle(int queueTime) {
        TextColor color = ChatFormat.Minecraft.green;

        if (queueTime <= 3 && queueTime > 1) {
            color = ChatFormat.Minecraft.yellow;
        } else if (queueTime <= 1) {
            color = ChatFormat.Minecraft.red;
        }

        return Title.title(Component.text(queueTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
    }

    public static void joinQueue(NexiaPlayer player) {
        BridgePlayerData data = (BridgePlayerData) PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(player);
        data.team = null;

        if(BridgeGame.isStarted){
            BridgeGame.spectator.add(player);
            ((BridgePlayerData)PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(player)).gameMode = BridgeGameMode.SPECTATOR;
            player.setGameMode(Minecraft.GameMode.SPECTATOR);
        } else {
            BridgeGame.queue.add(player);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }

        player.unwrap().teleportTo(world, 0, 101, 0, 0, 0);
        player.unwrap().setRespawnPosition(world.dimension(), new BlockPos(0, 100, 0), 0, true, false);
    }

    public static void endGame(BridgeTeam winnerTeam) {
        BridgeGame.isEnding = true;

        if(winnerTeam == null) {
            Component msg = Component.text("The game was a ")
                    .color(ChatFormat.normalColor)
                    .append(Component.text("draw", ChatFormat.brandColor2))
                    .append(Component.text("!", ChatFormat.normalColor)
                    );
            for(NexiaPlayer player : BridgeGame.getViewers()){
                if(player != null && player.unwrap() != null) player.sendTitle(Title.title(msg, Component.text("")));
            }

            return;
        }

        BridgeGame.winnerTeam = winnerTeam;

        int teamID = 1;
        if(winnerTeam == BridgeGame.team2) teamID = 2;

        for(NexiaPlayer player : winnerTeam.players) {
            player.sendTitle(Title.title(Component.text("You won!").color(ChatFormat.greenColor), Component.text("")));
            PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(player).savedData.incrementInteger("wins");
        }

        for(NexiaPlayer player : BridgeGame.getViewers()){
            player.sendTitle(Title.title(Component.text("Team " + teamID, ChatFormat.brandColor2), Component.text("has won the game! (" + winnerTeam.goals + " goals)", ChatFormat.normalColor)));
        }
    }

    public static void updateInfo() {

        String[] timer = TickUtil.minuteTimeStamp(BridgeGame.gameTime * 20);
        for(NexiaPlayer player : BridgeGame.getViewers()) {
            if(player == null) return;
            BridgeTeam playerTeam = ((BridgePlayerData)PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(player)).team;
            if(playerTeam == null) playerTeam = BridgeGame.team1; // maybe cuz spectator
            BridgeTeam otherTeam = BridgeGame.team1;
            if(playerTeam.equals(BridgeGame.team1)) otherTeam = BridgeGame.team2;

            player.sendActionBarMessage(
                    Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                            .append(Component.text(BridgeGame.map.name, ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(timer[0] + ":" + timer[1], ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Goals » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(playerTeam.goals + "/" + BridgeGame.map.maxGoals, ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Enemy Team Goals » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(otherTeam.goals + "/" + BridgeGame.map.maxGoals, ChatFormat.brandColor2))
            );
        }
    }

//    private static ArmorStand createArmorStand() {
//        ArmorStand armorStand = new ArmorStand(BridgeGame.world, 0, 81, 0);
//        armorStand.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 5, true, true));
//        DyeableLeatherItem leatherItem = (DyeableLeatherItem) Items.LEATHER_CHESTPLATE;
//
//        ItemStack helmet = Items.LEATHER_HELMET.getDefaultInstance();
//        helmet.getOrCreateTag().putInt("Unbreakable", 1);
//
//        ItemStack chestplate = Items.LEATHER_CHESTPLATE.getDefaultInstance();
//        chestplate.getOrCreateTag().putInt("Unbreakable", 1);
//
//        ItemStack leggings = Items.LEATHER_LEGGINGS.getDefaultInstance();
//        leggings.getOrCreateTag().putInt("Unbreakable", 1);
//
//        ItemStack boots = Items.LEATHER_BOOTS.getDefaultInstance();
//        boots.getOrCreateTag().putInt("Unbreakable", 1);
//
//        // r * 65536 + g * 256 + b
//        int black = 0;
//        int white = 255 * 65536 + 255 * 256 + 255;
//
//        leatherItem.setColor(helmet, white);
//        leatherItem.setColor(chestplate, black);
//        leatherItem.setColor(leggings, white);
//        leatherItem.setColor(boots, black);
//
//        armorStand.setItemSlot(EquipmentSlot.HEAD, helmet);
//        armorStand.setItemSlot(EquipmentSlot.CHEST, chestplate);
//        armorStand.setItemSlot(EquipmentSlot.LEGS, leggings);
//        armorStand.setItemSlot(EquipmentSlot.FEET, boots);
//
//        //armorStand.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0.0);
//
//        armorStand.setNoBasePlate(true);
//        //armorStand.setInvisible(true);
//
//        BridgeGame.world.addFreshEntity(armorStand);
//
//        return armorStand;
//    }

    private static BridgeTeam assignPlayer(NexiaPlayer player) {
        int players = BridgeGame.players.size();
        int random = RandomUtil.randomInt(1, 2);

        int team1 = BridgeGame.team1.players.size();
        int team2 = BridgeGame.team2.players.size();

        if(team1 >= players/2) {
            BridgeGame.team2.addPlayer(player);
            return BridgeGame.team2;
        } else if (team2 >= players/2) {
            BridgeGame.team1.addPlayer(player);
            return BridgeGame.team1;
        }

        if(random == 1) {
            BridgeGame.team1.addPlayer(player);
            return BridgeGame.team1;
        } else if (random == 2) {
            BridgeGame.team2.addPlayer(player);
            return BridgeGame.team2;
        }

        return null;
    }

    public static void startGame() {
        if(BridgeGame.queueTime <= 0){
            BridgeGame.isStarted = true;
            BridgeGame.gameTime = 600;
            BridgeGame.players.addAll(BridgeGame.queue);

            // leather armor dyed blue/red depending on the team

//            ItemStack kicking = new ItemStack(Items.NETHERITE_SWORD);
//            kicking.getOrCreateTag().putBoolean("Unbreakable", true);
//            kicking.enchant(Enchantments.KNOCKBACK, 4);
//            kicking.setHoverName(new TextComponent("§7§lKicking §7Sword §8[§710s cooldown§8]"));
//            kicking.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);



            for(NexiaPlayer player : BridgeGame.players) {


                BridgePlayerData data = (BridgePlayerData) PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(player);
                data.gameMode = BridgeGameMode.PLAYING;

                player.addTag("in_bridge_game");
//                player.addTag(LobbyUtil.NO_DAMAGE_TAG);
//                player.unwrap().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 255, false, false, false));

                data.team = BridgeGame.assignPlayer(player);
                while(data.team == null) {
                    data.team = BridgeGame.assignPlayer(player);
                    // if you're still null then im going to beat the shit out of you
                }
                data.team.spawnPosition.teleportPlayer(BridgeGame.world, player.unwrap());

//                ItemStack helmet = Items.LEATHER_HELMET.getDefaultInstance();
//                helmet.getOrCreateTag().putInt("Unbreakable", 1);

//                ItemStack chestplate = Items.LEATHER_CHESTPLATE.getDefaultInstance();
//                chestplate.getOrCreateTag().putInt("Unbreakable", 1);
//
//                ItemStack leggings = Items.LEATHER_LEGGINGS.getDefaultInstance();
//                leggings.getOrCreateTag().putInt("Unbreakable", 1);
//
//                ItemStack boots = Items.LEATHER_BOOTS.getDefaultInstance();
//                boots.getOrCreateTag().putInt("Unbreakable", 1);
//
//                int colour = 0;
//
//                if(data.team.equals(BridgeGame.team1)) {
//                    // r * 65536 + g * 256 + b
//                    colour = 255 * 65536;
//                } else if(data.team.equals(BridgeGame.team2)) {
//                    colour = 255;
//                }
//
//                DyeableLeatherItem leatherItem = (DyeableLeatherItem) Items.LEATHER_CHESTPLATE;
//
////                leatherItem.setColor(helmet, colour);
//                leatherItem.setColor(chestplate, colour);
//                leatherItem.setColor(leggings, colour);
//                leatherItem.setColor(boots, colour);
//
////                player.unwrap().setItemSlot(EquipmentSlot.HEAD, helmet);
//                player.unwrap().setItemSlot(EquipmentSlot.CHEST, chestplate);
//                player.unwrap().setItemSlot(EquipmentSlot.LEGS, leggings);
//                player.unwrap().setItemSlot(EquipmentSlot.FEET, boots);
//
//                ItemStack sword = Items.IRON_SWORD.getDefaultInstance();
//                sword.getOrCreateTag().putInt("Unbreakable", 1);
//

                giveKit(player)


                player.setGameMode(Minecraft.GameMode.SURVIVAL);
                //player.setRespawnPosition(world.dimension(), pos, 0, true, false);
//                player.unwrap().getCooldowns().addCooldown(Items.NETHERITE_SWORD, 200);
            }

            BridgeGame.spectator.clear();
            BridgeGame.queue.clear();
        }
    }

    public static boolean isBridgePlayer(NexiaPlayer player){
        return ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode == PlayerGameMode.BRIDGE || player.hasTag("bridge") || player.hasTag("in_bridge_game");
    }

    public static void resetAll() {
        queue.clear();
        players.clear();
        spectator.clear();

        map = BridgeMap. bridgeMaps.get(RandomUtil.randomInt(BridgeMap.bridgeMaps.size()));
        world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("bridge", BridgeGame.map.id), WorldUtil.defaultWorldConfig).asWorld();

        isStarted = false;
        queueTime = 15;
        gameTime = 600;
        isEnding = false;
        team1 = new BridgeTeam(new ArrayList<>(), map.team1Pos);
        team2 = new BridgeTeam(new ArrayList<>(), map.team2Pos);
        winnerTeam = null;
        endTime = 5;
    }

    public static void tick() {
        if(BridgeGame.world == null) return;
        if(BridgeGame.world.players().isEmpty()) return;

        AABB aabb = new AABB(BridgeGame.map.corner1, BridgeGame.map.corner2);
        Predicate<Entity> predicate = o -> true;

//        for (ItemEntity entity : BridgeGame.world.getEntities(EntityType.ITEM, aabb, predicate)) {
//            // kill @e[type=item,distance=0..]
//            entity.remove();
//        }

//        if(!BridgeGame.isStarted) {
//            for (ArmorStand entity : BridgeGame.world.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
//                // kill @e[type=item,distance=0..]
//                entity.remove();
//            }
//            return;
//        }

        // check if armor stand (football) is in goal, then reset football to middle and give goal

        aabb = new AABB(BridgeGame.map.team1goalCorner1, BridgeGame.map.team1goalCorner2);
        for (NexiaPlayer player : BridgeGame.world.getPlayers(EntityType.PLAYER, team2, aabb, predicate)) {
            BridgeGame.goal(player, BridgeGame.team2);
        }
        aabb = new AABB(BridgeGame.map.team2goalCorner1, BridgeGame.map.team2goalCorner2);
        for (NexiaPlayer player : BridgeGame.world.getPlayer(EntityType.PLAYER, team1, aabb, predicate)) {
            BridgeGame.goal(player, BridgeGame.team1);
        }
    }

    public static void firstTick(){
        resetAll();
    }

    public static ArrayList<NexiaPlayer> getViewers() {
        ArrayList<NexiaPlayer> viewers = new ArrayList<>();
        viewers.addAll(BridgeGame.players);
        viewers.addAll(BridgeGame.spectator);
        return viewers;
    }
}
