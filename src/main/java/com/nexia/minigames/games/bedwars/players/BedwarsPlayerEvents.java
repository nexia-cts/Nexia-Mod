package com.nexia.minigames.games.bedwars.players;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.bedwars.custom.BedwarsTrident;
import com.nexia.minigames.games.bedwars.util.BedwarsUtil;
import com.nexia.minigames.games.bedwars.util.player.BedwarsPlayerData;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.BedwarsGame;
import com.nexia.minigames.games.bedwars.custom.BedwarsBridgeEgg;
import com.nexia.minigames.games.bedwars.custom.BedwarsExplosiveSlime;
import com.nexia.minigames.games.bedwars.shop.BedwarsShop;
import com.nexia.minigames.games.bedwars.shop.BedwarsShopUpgradeables;
import com.nexia.minigames.games.bedwars.upgrades.BedwarsUpgradeShop;
import com.nexia.minigames.games.bedwars.util.BedwarsScoreboard;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BedwarsPlayerEvents {

    public static void tryToJoin(NexiaPlayer player, boolean throughEvent) {
        if (BedwarsUtil.isInBedWars(player)) {
            //player.sendMessage(Component.text("You are already in the game.").color(ChatFormat.failColor));
            LobbyUtil.returnToLobby(player, false);
            tryToJoin(player, throughEvent);
            return;
        }
        if (BedwarsGame.queueList.size() >= BedwarsGame.maxPlayerCount) {
            player.sendMessage(Component.text("The game is full.").color(ChatFormat.failColor));
            LobbyUtil.returnToLobby(player, false);
            return;
        }
        if (BedwarsGame.isGameActive) {
            BedwarsPlayers.becomeSpectator(player);
            return;
        }

        BedwarsPlayers.joinQueue(player);
    }

    public static boolean spectatorTeleport(NexiaPlayer player, ServerboundTeleportToEntityPacket packet) {
        if (BedwarsUtil.isBedWarsPlayer(player)) {
            player.sendMessage(Component.text("You can't spectate others while in the game.").color(ChatFormat.failColor));
            return false;
        }

        for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
            Entity entity = packet.getEntity(serverLevel);
            if (!(entity instanceof ServerPlayer target)) continue;
            NexiaPlayer nexiaTarget = new NexiaPlayer(target);

            if (!BedwarsUtil.isBedWarsPlayer(nexiaTarget)) {
                player.sendMessage(Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                return false;
            }
        }
        return true;
    }

    public static void afterHurt(NexiaPlayer player, DamageSource damageSource) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player.unwrap());
        if (attacker != null) {
            ((BedwarsPlayerData)PlayerDataManager.getDataManager(NexiaCore.BEDWARS_DATA_MANAGER).get(player)).combatTagPlayer = attacker;
            ((BedwarsPlayerData)PlayerDataManager.getDataManager(NexiaCore.BEDWARS_DATA_MANAGER).get(attacker.getUUID())).combatTagPlayer = player.unwrap();
            if (player.unwrap().hasEffect(MobEffects.INVISIBILITY)) {
                player.unwrap().removeEffect(MobEffects.INVISIBILITY);
            }
        }
    }

    public static void death(NexiaPlayer player) {
        if (!BedwarsPlayers.getPlayers().contains(player) || BedwarsGame.winScreen) return;

        BedwarsUtil.giveKillResources(player);
        BedwarsUtil.deathClearInventory(player);
        BedwarsShopUpgradeables.downgradePlayerTools(player);

        if (BedwarsGame.gameTridents.containsKey(player)) {
            for (BedwarsTrident trident : BedwarsGame.gameTridents.get(player)) {
                trident.remove();
            }
            BedwarsGame.gameTridents.remove(player);
        }

        BedwarsUtil.announceDeath(player);

        BedwarsTeam team = BedwarsTeam.getPlayerTeam(player);
        if (team != null && BedwarsTeam.bedExists(team)) {
            player.setGameMode(Minecraft.GameMode.SPECTATOR);
            BedwarsGame.respawningList.put(player, BedwarsGame.respawnTime * 20);
            player.unwrap().setRespawnPosition(BedwarsAreas.bedWarsWorld.dimension(),
                    BedwarsAreas.spectatorSpawn.toBlockPos(), BedwarsAreas.spectatorSpawn.yaw, true, false);
        } else {
            BedwarsPlayers.eliminatePlayer(player, true);
        }
    }

    public static void respawned(NexiaPlayer player) {
        boolean fixedTeamPlayer = BedwarsTeam.fixTeamPlayer(player);

        if (!fixedTeamPlayer) {
            // Fix spectator
            for (int i = 0; i < BedwarsGame.spectatorList.size(); i++) {
                NexiaPlayer spectator = BedwarsGame.spectatorList.get(i);
                if (spectator.getUUID().equals(player.getUUID())) {
                    BedwarsGame.spectatorList.set(i, player);
                    return;
                }
            }
        }
    }

    // Usage of /leave command or disconnecting
    public static void leaveInBedWars(NexiaPlayer player) {
        if (BedwarsGame.queueList.contains(player)) {
            BedwarsPlayers.leaveQueue(player);
        } else if (BedwarsPlayers.getPlayers().contains(player)) {
            BedwarsPlayers.eliminatePlayer(player, false);
        } else BedwarsGame.spectatorList.remove(player);
        BedwarsScoreboard.removeScoreboardFor(player);
    }

    // Ability to shoot fireballs
    public static boolean useItem(ServerPlayer player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.getItem() == Items.FIRE_CHARGE) {
            BedwarsUtil.throwFireball(player, itemStack);
            return false;

        } else if (BedwarsExplosiveSlime.isBwExplosiveSlime(itemStack)) {
            BedwarsUtil.throwSlime(player, itemStack);
            return false;
        }

        return true;
    }

    public static boolean beforeStripWood(ServerPlayer player, UseOnContext context) {
        if (player.isCreative()) return true;

        BlockPos blockPos = context.getClickedPos();

        if (!BedwarsAreas.canBuildAt(new NexiaPlayer(player), blockPos, false)) {
            return false;
        }

        return true;
    }

    public static boolean beforePlace(NexiaPlayer player, BlockPlaceContext blockPlaceContext) {
        if (player.unwrap().isCreative()) return true;

        BlockPos blockPos = blockPlaceContext.getClickedPos();

        if (!BedwarsAreas.canBuildAt(player, blockPos, true)) {
            return false;
        }
        return !BedwarsUtil.placeTnt(player.unwrap(), blockPlaceContext);
    }

    public static boolean beforeBreakBlock(NexiaPlayer player, BlockPos blockPos) {
        if (player.unwrap().isCreative()) return true;

        BedwarsTeam team = BedwarsTeam.getPlayerTeam(player);
        BlockState blockState = BedwarsAreas.bedWarsWorld.getBlockState(blockPos);

        if (team != null && BlockUtil.blockToText(blockState).equals(team.color + "_bed")) {
            return false;
        }

        return BedwarsAreas.canBuildAt(player, blockPos, true);
    }

    public static void bedBroken(NexiaPlayer player, BlockPos blockPos) {
        for (BedwarsTeam team : BedwarsTeam.allTeams.values()) {
            if (team.bedLocation == null) continue;

            if (new EntityPos(team.bedLocation).isInRadius(new EntityPos(blockPos), 1)) {
                team.announceBedBreak(player, blockPos);
                PlayerDataManager.getDataManager(NexiaCore.BEDWARS_DATA_MANAGER).get(player).savedData.incrementInteger("bedsBroken");
                break;
            }
        }
    }

    public static void drankPotion(NexiaPlayer player, ItemStack potionItem) {
        for (MobEffectInstance effect : PotionUtils.getMobEffects(potionItem)) {
            if (effect.getEffect() == MobEffects.INVISIBILITY) {

                if (!BedwarsGame.invisiblePlayerArmor.containsKey(player)) {
                    BedwarsGame.invisiblePlayerArmor.put(player, player.unwrap().inventory.armor.toArray(new ItemStack[0]));
                }
                break;
            }
        }
    }

    public static ThrownEgg throwEgg(NexiaPlayer player, ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null || !compoundTag.getBoolean(BedwarsBridgeEgg.itemTagKey)) {
            return new ThrownEgg(player.unwrap().level, player.unwrap());
        }

        Block trail = Blocks.AIR;
        BedwarsTeam team = BedwarsTeam.getPlayerTeam(player);
        if (team != null) {
            trail = Registry.BLOCK.get(new ResourceLocation(team.color + "_wool"));
        }
        if (trail == Blocks.AIR) trail = Blocks.WHITE_WOOL;

        return new BedwarsBridgeEgg(player.unwrap().level, player, trail);
    }

    public static ThrownTrident throwTrident(NexiaPlayer player, ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null || !compoundTag.getBoolean(BedwarsTrident.itemTagKey)) {
            return new ThrownTrident(player.unwrap().level, player.unwrap(), itemStack);
        }

        return new BedwarsTrident(player.unwrap().level, player.unwrap(), itemStack);
    }

    public static boolean interact(Player player, ServerboundInteractPacket serverboundInteractPacket) {
        if (serverboundInteractPacket.getAction() != ServerboundInteractPacket.Action.INTERACT) return true;
        if (BedwarsShop.bedWarsShopItems == null) return true;
        if (!(player instanceof ServerPlayer)) return true;

        Entity entity = serverboundInteractPacket.getTarget(player.level);
        if (!(entity instanceof Villager)) return true;

        if (entity.getTags().contains("bedWarsShop_item")) {
            BedwarsShop.openShopGui((ServerPlayer) player);
            return false;
        }
        if (entity.getTags().contains("bedWarsShop_upgrade")) {
            BedwarsUpgradeShop.openShopGui((ServerPlayer) player);
            return false;
        }

        return true;
    }

    public static boolean containerClick(NexiaPlayer player, ServerboundContainerClickPacket packet) {
        int containerId = packet.getContainerId();
        int slot = packet.getSlotNum();

        if (containerId == 0 && slot >= 5 && slot <= 8) {
            return false;
        }

        ItemStack itemStack = ItemStackUtil.getContainerClickItem(player.unwrap(), packet);

        if ((itemStack != null) && (packet.getClickType() == ClickType.THROW || slot == -999)) {
            return BedwarsUtil.canDropItem(itemStack);
        }

        return true;
    }

    public static void afterHungerTick(FoodData foodData) {
        foodData.setFoodLevel(20);
    }

}
