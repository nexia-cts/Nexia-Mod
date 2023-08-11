package com.nexia.minigames.games.bedwars.util;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.custom.BwExplosiveSlime;
import com.nexia.minigames.games.bedwars.players.BwPlayers;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.*;

public class BwUtil {

    static HashMap<UUID, Integer> explosiveCooldown = new HashMap<>();

    public static void utilTick() {
        invisibilityTick();
        explosionTick();
    }

    private static void invisibilityTick() {
        for (Iterator<ServerPlayer> it = BwGame.invisiblePlayerArmor.keySet().iterator(); it.hasNext(); ) {
            ServerPlayer player = it.next();
            invisArmorCheck(player);
            if (!player.hasEffect(MobEffects.INVISIBILITY)) {
                regainInvisArmor(player);
                it.remove();
            }
        }
    }

    private static void explosionTick() {
        for (Iterator<UUID> it = explosiveCooldown.keySet().iterator(); it.hasNext(); ) {
            UUID uuid = it.next();
            explosiveCooldown.replace(uuid, explosiveCooldown.get(uuid) - 1);
            if (explosiveCooldown.get(uuid) <= 0) {
                it.remove();
            }
        }
    }

    public static boolean skipQueue() {
        if (BwGame.queueCountdown > 1) {
            BwGame.queueCountdown = 1;
            return true;
        }
        return false;
    }

    public static void createSpectatorTeam() {
        MinecraftServer server = ServerTime.minecraftServer;
        ServerScoreboard scoreboard = server.getScoreboard();

        String teamName = "bw_spectator";
        PlayerTeam playerTeam = scoreboard.getPlayerTeam(teamName);
        if (scoreboard.getPlayerTeam(teamName) == null) {
            playerTeam = scoreboard.addPlayerTeam(teamName);
        }

        playerTeam.setDisplayName(new TextComponent(teamName));
        playerTeam.setPlayerPrefix(new TextComponent("\2477\247lBW " ));

        server.getCommands().performCommand(server.createCommandSourceStack(),
                "team modify " + teamName + " color gray");

        BwGame.spectatorTeam = playerTeam;
    }

    public static void throwSlime(ServerPlayer player, ItemStack itemStack) {
        if (explosiveCooldown.containsKey(player.getUUID())) return;

        new BwExplosiveSlime(EntityType.SLIME, player);
        itemStack.shrink(1);

        explosiveCooldown.put(player.getUUID(), 10);
    }

    public static void throwFireball(ServerPlayer player, ItemStack itemStack) {
        if (explosiveCooldown.containsKey(player.getUUID())) return;

        Level level = player.level;
        Vec3 pos = player.position().add(0, 1, 0);
        Vec3 angle = player.getLookAngle();
        float speed = 0.8f;

        LargeFireball fireball = new LargeFireball(level, player, angle.x, angle.y, angle.z);
        fireball.teleportTo(pos.x, pos.y, pos.z);
        fireball.setDeltaMovement(new Vec3(angle.x*speed, angle.y*speed, angle.z*speed));
        fireball.explosionPower = 2;

        level.addFreshEntity(fireball);
        itemStack.setCount(itemStack.getCount() - 1);
        explosiveCooldown.put(player.getUUID(), 10);
    }

    private static void invisArmorCheck(ServerPlayer player) {
        ItemStack[] storedArmor = BwGame.invisiblePlayerArmor.get(player);
        if (storedArmor == null) return;
        List<ItemStack> currentArmor = player.inventory.armor;

        for (int i = 0; i < currentArmor.size() && i < storedArmor.length; i++) {
            if (!currentArmor.get(i).isEmpty()) {
                storedArmor[i] = currentArmor.get(i);
                player.inventory.setItem(36 + i, ItemStack.EMPTY);
            }
        }
    }

    private static void regainInvisArmor(ServerPlayer player) {
        ItemStack[] armor = BwGame.invisiblePlayerArmor.get(player);
        if (armor == null) return;

        for (int i = 0; i < armor.length; i++) {
            player.inventory.setItem(36 + i, armor[i]);
        }
    }

    // Fuse tnt when placed in bedwars
    public static boolean placeTnt(Player player, BlockPlaceContext blockPlaceContext) {
        if (!blockPlaceContext.getItemInHand().getItem().toString().equals("tnt")) return false;

        ItemStack itemStack = blockPlaceContext.getItemInHand();
        if (!player.abilities.instabuild) itemStack.shrink(1);
        explodeTnt(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
        return true;
    }

    // Summon fused tnt
    private static void explodeTnt(Level level, BlockPos blockPos) {
        if (level.isClientSide) return;
        PrimedTnt primedTnt = new PrimedTnt(level, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, null);
        primedTnt.setFuse(60);
        level.addFreshEntity(primedTnt);
        level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public static boolean dropResources(BlockState blockState) {
        return !BlockUtil.blockToText(blockState).endsWith("_bed");
    }

    public static boolean canDropItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof PickaxeItem || item instanceof AxeItem || item instanceof ShearsItem) {
            return false;
        }
        if (BwUtil.isDefaultSword(itemStack)) {
            return false;
        }
        return true;
    }

    public static boolean canDropItem(com.combatreforged.factory.api.world.item.ItemStack itemStack) {
        String item = itemStack.getDisplayName().toString().toLowerCase();
        if (item.contains("pickaxe") || item.contains("axe")|| item.contains("shears")) {
            return false;
        }
        if (item.contains("stone sword")) {
            return false;
        }
        return true;
    }

    public static float playerArmorCalculation(ServerPlayer player, DamageSource damageSource, float damage) {

        if (damageSource.getEntity() instanceof ServerPlayer attacker) {
            float crit = PlayerUtil.couldCrit(attacker) ? 1.5f : 1f;
            float nonCritDamage = damage / crit;

            // Nerf stronger weapons
            float strongDamageNerf = 0.75f; // 1 if vanilla, less to nerf, more to buff
            float strongDamage = 4f; // At what point a weapon is considered strong

            if (nonCritDamage > strongDamage) {
                nonCritDamage = strongDamage * (1 - strongDamageNerf) + strongDamageNerf * nonCritDamage;
            }

            damage = nonCritDamage * crit;
        }

        if (!damageSource.isBypassArmor()) {
            // Calculation without armor penetration
            float protection = Mth.clamp(player.getArmorValue(), 0, 20);
            damage = damage * (1.0f - protection / 25.0f);
            // Make fights longer by buffing armor
            damage *= 0.9f;
        }
        return damage;
    }

    public static float playerProtCalculation(float originalDamage, float protection) {
        protection = Mth.clamp(protection, 0.0f, 20.0f);
        return originalDamage * (1.0f - protection / 48.0f);
    }

    public static float getPearlDamage() {
        return 0f;
    }

    public static float modifyBlockExplosionRes(BlockPos blockPos, BlockState blockState, Entity source, float resistance) {
        if (source instanceof PrimedTnt) resistance *= 1.125F;
        if (source instanceof BwExplosiveSlime) resistance *= 2f;

        if (BwAreas.isImmuneBlock(blockPos)) {
            if (resistance < 3F) resistance = 3F;

        } else if (blockState.getBlock() == Blocks.END_STONE) {
            //if (source instanceof LargeFireball || source instanceof BwExplosiveSlime) resistance = 1200F;
            if (!(source instanceof LargeFireball) && !(source instanceof BwExplosiveSlime)) resistance = 1200F;
            else resistance *= 0.75F;

        } else if (BlockUtil.blockToText(blockState).endsWith("_wool")) {
            resistance *= 0.75F;

        } else if (BlockUtil.blockToText(blockState).endsWith("_stained_glass")) {
            resistance = 1200F;
        }

        return resistance;
    }

    public static boolean shouldExplode(BlockPos blockPos, BlockState blockState) {
        //return !(BwAreas.isImmuneBlock(blockPos) || blockState.getBlock() instanceof BedBlock);
        return !BwAreas.isImmuneBlock(blockPos) && !(blockState.getBlock() instanceof BedBlock);
    }

    public static float getExplosionDamage(float original) {
        return 0.2F * original;
    }

    public static void modifyExplosionKb(Entity source, Args kb) {
        double horizontal = 1;
        double vertical = 1;

        // Arg 0 is x, arg 1 is y, arg 2 is z
        if (source instanceof LargeFireball) {
            vertical *= 1.5f;
        } else if (source instanceof PrimedTnt) {
            horizontal *= 3.5f;
            vertical *= 1.5f;
        } else if (source instanceof BwExplosiveSlime) {
            vertical *= 1.75f;
            horizontal *= 1.75f;
        }

        kb.set(0, (double)kb.get(0) * horizontal);
        kb.set(1, (double)kb.get(1) * vertical);
        kb.set(2, (double)kb.get(2) * horizontal);
    }

    public static void setAttackSpeed(ServerPlayer player) {
        UUID hasteUuid = UUID.fromString("AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3");
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeModifier modifier = attackSpeed.getModifier(hasteUuid);

        if (modifier != null) {
            attackSpeed.removeModifier(hasteUuid);
        }
        player.connection.send(new ClientboundUpdateAttributesPacket(
                player.getId(), player.getAttributes().getSyncableAttributes()));
    }

    public static void giveKillResources(ServerPlayer victim) {
        LivingEntity killCredit = victim.getKillCredit();
        if (killCredit instanceof ServerPlayer attacker) {
            Inventory inventory = victim.inventory;
            for (int i = 0; i < 36; i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (isBedWarsCurrency(itemStack)) {
                    attacker.inventory.add(itemStack);
                }
            }
        }
    }

    public static void deathClearInventory(ServerPlayer player) {
        Inventory inventory2 = player.inventory;
        for (int i = 0; i < inventory2.items.size(); i++) {
            if (!isTool(inventory2.items.get(i))) {
                inventory2.items.set(i, new ItemStack(Items.AIR));
            }
        }
        if (!isTool(inventory2.offhand.get(0))) inventory2.offhand.set(0, new ItemStack(Items.AIR));
    }

    public static void afterItemMerge(ItemEntity itemEntity) {
        for (String tag : itemEntity.getTags()) {
            if (!tag.startsWith(BwGen.itemCapTagStart)) continue;
            try {
                int cap = Integer.parseInt(tag.replace(BwGen.itemCapTagStart, ""));
                if (itemEntity.getItem().getCount() > cap) {
                    itemEntity.getItem().setCount(cap);
                }
            } catch (Exception e) {
                break;
            }

        }
    }

    public static void announceDeath(ServerPlayer player) {
        String mainColor = LegacyChatFormat.chatColor2;
        String message = mainColor + player.getCombatTracker().getDeathMessage().getString();

        message = replaceDisplayName(message, mainColor, player);

        Entity killCredit = player.getKillCredit();
        if (killCredit instanceof ServerPlayer attacker) {
            message = replaceDisplayName(message, mainColor, attacker);
        }

        PlayerUtil.broadcast(BwPlayers.getViewers(), message);
    }

    public static String replaceDisplayName(String message, String mainColor, ServerPlayer player) {
        if (player == null) return message;

        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team == null) return message;

        return message.replace(player.getDisplayName().getString(),
                team.textColor + player.getScoreboardName() + mainColor);
    }

    public static float getFireballInertia() {
        return 0.875f;
    }

    public static boolean isDefaultSword(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() == Items.STONE_SWORD;
    }

    static boolean isTool(ItemStack itemStack) {
        String itemString = itemStack.getItem().toString();
        return itemString.endsWith("axe") || itemStack.getItem() == Items.SHEARS;
    }

    public static boolean isBedWarsCurrency(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item == Items.IRON_INGOT || item == Items.GOLD_INGOT || item == Items.DIAMOND || item == Items.EMERALD;
    }

    public static boolean isInBedWars(ServerPlayer player) {
        return PlayerDataManager.get(player).gameMode == PlayerGameMode.BEDWARS;
    }

    public static boolean isBedWarsPlayer(ServerPlayer player) {
        if (!isInBedWars(player)) return false;
        for (ServerPlayer serverPlayer : BwPlayers.getPlayers()) {
            if (serverPlayer.getUUID().equals(player.getUUID())) return true;
        }
        return false;
    }

}
