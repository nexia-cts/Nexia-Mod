package com.nexia.minigames.games.bedwars.players;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.custom.BwBridgeEgg;
import com.nexia.minigames.games.bedwars.custom.BwExplosiveSlime;
import com.nexia.minigames.games.bedwars.custom.BwTrident;
import com.nexia.minigames.games.bedwars.shop.BwShop;
import com.nexia.minigames.games.bedwars.shop.BwShopUpgradeables;
import com.nexia.minigames.games.bedwars.upgrades.BwUpgradeShop;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BwPlayerEvents {

    public static void tryToJoin(ServerPlayer player, boolean throughEvent) {

        com.combatreforged.factory.api.world.entity.player.Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

        if (BwUtil.isInBedWars(player)) {
            factoryPlayer.sendMessage(Component.text("You are already in the game.").color(ChatFormat.failColor));
            return;
        }
        if (BwGame.queueList.size() >= BwGame.maxPlayerCount) {
            factoryPlayer.sendMessage(Component.text("The game is full.").color(ChatFormat.failColor));
            return;
        }
        if (BwGame.isGameActive) {
            BwPlayers.becomeSpectator(player);
            return;
        }

        BwPlayers.joinQueue(player);
    }

    public static boolean spectatorTeleport(ServerPlayer player, ServerboundTeleportToEntityPacket packet) {

        com.combatreforged.factory.api.world.entity.player.Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

        if (BwUtil.isBedWarsPlayer(player)) {
            factoryPlayer.sendMessage(Component.text("You can't spectate others while in the game.").color(ChatFormat.failColor));
            return false;
        }

        for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
            Entity entity = packet.getEntity(serverLevel);
            if (!(entity instanceof ServerPlayer target)) continue;

            if (!BwUtil.isBedWarsPlayer(target)) {
                factoryPlayer.sendMessage(Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                return false;
            }
        }
        return true;
    }

    public static void afterHurt(ServerPlayer player, DamageSource damageSource) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(damageSource.getEntity());
        if (attacker != null) {
            PlayerDataManager.get(player).combatTagPlayer = attacker;
            PlayerDataManager.get(attacker).combatTagPlayer = player;
            if (player.hasEffect(MobEffects.INVISIBILITY)) {
                player.removeEffect(MobEffects.INVISIBILITY);
            }
        }
    }

    public static void death(ServerPlayer player) {
        if (!BwPlayers.getPlayers().contains(player) || BwGame.winScreen) return;

        BwUtil.giveKillResources(player);
        BwUtil.deathClearInventory(player);
        BwShopUpgradeables.downgradePlayerTools(player);

        if (BwGame.gameTridents.containsKey(player)) {
            for (BwTrident trident : BwGame.gameTridents.get(player)) {
                trident.remove();
            }
            BwGame.gameTridents.remove(player);
        }

        BwUtil.announceDeath(player);

        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team != null && BwTeam.bedExists(team)) {
            player.setGameMode(GameType.SPECTATOR);
            BwGame.respawningList.put(player, BwGame.respawnTime * 20);
            player.setRespawnPosition(BwAreas.bedWarsWorld.dimension(),
                    BwAreas.spectatorSpawn.toBlockPos(), BwAreas.spectatorSpawn.yaw, true, false);
        } else {
            BwPlayers.eliminatePlayer(player, true);
        }
    }

    public static void respawned(ServerPlayer player) {
        boolean fixedTeamPlayer = BwTeam.fixTeamPlayer(player);

        if (!fixedTeamPlayer) {
            // Fix spectator
            for (int i = 0; i < BwGame.spectatorList.size(); i++) {
                ServerPlayer spectator = BwGame.spectatorList.get(i);
                if (spectator.getUUID().equals(player.getUUID())) {
                    BwGame.spectatorList.set(i, player);
                    return;
                }
            }
        }
    }

    // Usage of /leave command or disconnecting
    public static void leaveInBedWars(ServerPlayer player) {
        if (BwGame.queueList.contains(player)) {
            BwPlayers.leaveQueue(player);
        } else if (BwPlayers.getPlayers().contains(player)) {
            BwPlayers.eliminatePlayer(player, false);
        } else if (BwGame.spectatorList.contains(player)) {
            BwGame.spectatorList.remove(player);
            BwScoreboard.removeScoreboardFor(player);
        }
    }

    // Ability to shoot fireballs
    public static boolean useItem(ServerPlayer player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.getItem() == Items.FIRE_CHARGE) {
            BwUtil.throwFireball(player, itemStack);
            return false;

        } else if (BwExplosiveSlime.isBwExplosiveSlime(itemStack)) {
            BwUtil.throwSlime(player, itemStack);
            return false;
        }

        return true;
    }

    public static boolean beforeStripWood(ServerPlayer player, UseOnContext context) {
        if (player.isCreative()) return true;

        BlockPos blockPos = context.getClickedPos();

        if (!BwAreas.canBuildAt(player, blockPos, false)) {
            return false;
        }

        return true;
    }

    public static boolean beforePlace(ServerPlayer player, BlockPlaceContext blockPlaceContext) {
        if (player.isCreative()) return true;

        BlockPos blockPos = blockPlaceContext.getClickedPos();

        if (!BwAreas.canBuildAt(player, blockPos, true)) {
            return false;
        }
        if (BwUtil.placeTnt(player, blockPlaceContext)) {
            return false;
        }

        return true;
    }

    public static boolean beforeBreakBlock(ServerPlayer player, BlockPos blockPos) {
        if (player.isCreative()) return true;

        BwTeam team = BwTeam.getPlayerTeam(player);
        BlockState blockState = BwAreas.bedWarsWorld.getBlockState(blockPos);

        if (team != null && BlockUtil.blockToText(blockState).equals(team.color + "_bed")) {
            return false;
        }

        if (!BwAreas.canBuildAt(player, blockPos, true)) {
            return false;
        }

        return true;
    }

    public static void bedBroken(ServerPlayer player, BlockPos blockPos) {
        for (BwTeam team : BwTeam.allTeams.values()) {
            if (team.bedLocation == null) continue;

            if (new EntityPos(team.bedLocation).isInRadius(new EntityPos(blockPos), 1)) {
                team.announceBedBreak(player, blockPos);
                break;
            }
        }
    }

    public static void drankPotion(ServerPlayer player, ItemStack potionItem) {
        for (MobEffectInstance effect : PotionUtils.getMobEffects(potionItem)) {
            if (effect.getEffect() == MobEffects.INVISIBILITY) {

                if (!BwGame.invisiblePlayerArmor.containsKey(player)) {
                    BwGame.invisiblePlayerArmor.put(player, player.inventory.armor.toArray(new ItemStack[0]));
                }
                break;
            }
        }
    }

    public static ThrownEgg throwEgg(ServerPlayer player, ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null || !compoundTag.getBoolean(BwBridgeEgg.itemTagKey)) {
            return new ThrownEgg(player.level, player);
        }

        Block trail = Blocks.AIR;
        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team != null) {
            trail = Registry.BLOCK.get(new ResourceLocation(team.color + "_wool"));
        }
        if (trail == Blocks.AIR) trail = Blocks.WHITE_WOOL;

        return new BwBridgeEgg(player.level, player, trail);
    }

    public static ThrownTrident throwTrident(ServerPlayer player, ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null || !compoundTag.getBoolean(BwTrident.itemTagKey)) {
            return new ThrownTrident(player.level, player, itemStack);
        }

        return new BwTrident(player.level, player, itemStack);
    }

    public static boolean interact(Player player, ServerboundInteractPacket serverboundInteractPacket) {
        if (serverboundInteractPacket.getAction() != ServerboundInteractPacket.Action.INTERACT) return true;
        if (BwShop.bedWarsShopItems == null) return true;
        if (!(player instanceof ServerPlayer)) return true;

        Entity entity = serverboundInteractPacket.getTarget(player.level);
        if (!(entity instanceof Villager)) return true;

        if (entity.getTags().contains("bedWarsShop_item")) {
            BwShop.openShopGui((ServerPlayer) player);
            return false;
        }
        if (entity.getTags().contains("bedWarsShop_upgrade")) {
            BwUpgradeShop.openShopGui((ServerPlayer) player);
            return false;
        }

        return true;
    }

    public static boolean containerClick(ServerPlayer player, ServerboundContainerClickPacket packet) {
        int containerId = packet.getContainerId();
        int slot = packet.getSlotNum();

        if (containerId == 0 && slot >= 5 && slot <= 8) {
            return false;
        }

        ItemStack itemStack = ItemStackUtil.getContainerClickItem(player, packet);

        if ((itemStack != null) && (packet.getClickType() == ClickType.THROW || slot == -999)) {
            if (!BwUtil.canDropItem(itemStack)) return false;
        }

        return true;
    }

    public static void afterHungerTick(FoodData foodData) {
        foodData.setFoodLevel(20);
    }

}
