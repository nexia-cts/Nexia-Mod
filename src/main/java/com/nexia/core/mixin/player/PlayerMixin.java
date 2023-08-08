package com.nexia.core.mixin.player;

import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow @Final public Inventory inventory;

    @Shadow public abstract ItemCooldowns getCooldowns();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }


    /**
     * @author NotCoded
     * @reason Make shield break actually play the sound to other players.
     */
    @Overwrite
    public boolean disableShield(float f) {
        this.getCooldowns().addCooldown(Items.SHIELD, (int)(f * 20.0F));
        this.stopUsingItem();
        this.level.broadcastEntityEvent(this, (byte)30);
        if((this.getLastDamageSource() != null && this.getLastDamageSource().getEntity() != null && PlayerUtil.getPlayerAttacker(this.getLastDamageSource().getEntity()) != null) && Main.config.enhancements.betterShields){
            //this.level.broadcastEntityEvent(attacker, (byte)30);
            ServerPlayer attacker = PlayerUtil.getPlayerAttacker(this.getLastDamageSource().getEntity());

            SoundSource soundSource = null;
            for (SoundSource source : SoundSource.values()) {
                soundSource = source;
            }
            PlayerUtil.sendSound(attacker, new EntityPos(attacker.position()), SoundEvents.SHIELD_BREAK, soundSource, 2, 1);
        }
        return true;
    }

    @Inject(method = "hurt", cancellable = true, at = @At("HEAD"))
    private void beforeHurt(DamageSource damageSource, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        if(player.getLevel().equals(LobbyUtil.lobbyWorld) && damageSource == DamageSource.OUT_OF_WORLD) LobbyUtil.lobbySpawn.teleportPlayer(LobbyUtil.lobbyWorld, player);

        if (player.getTags().contains(LobbyUtil.NO_DAMAGE_TAG)) {
            cir.setReturnValue(false);
            return;
        }

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(damageSource.getEntity());
        if(attacker != null) {
            if(attacker.getTags().contains(LobbyUtil.NO_DAMAGE_TAG)) cir.setReturnValue(false);

            DuelsTeam team = PlayerDataManager.get(player).duelsTeam;
            if(team != null && team.all.contains(attacker) && com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY) cir.setReturnValue(false);
        }
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void afterHurt(DamageSource damageSource, float damage, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        if (BwUtil.isBedWarsPlayer(player)) {
            BwPlayerEvents.afterHurt(player, damageSource);
        }
    }

    @Redirect(method = "actuallyHurt",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float modifyArmorCalculation(Player instance, DamageSource damageSource, float damage) {

        hurtArmor(damageSource, damage);
        if (!((Object) this instanceof ServerPlayer player)) return vanillaArmorCalculation(damageSource, damage);

        if (BwUtil.isBedWarsPlayer(player)) {
            return BwUtil.playerArmorCalculation(player, damageSource, damage);
        }

        return vanillaArmorCalculation(damageSource, damage);
    }

    public float vanillaArmorCalculation(DamageSource damageSource, float damage) {
        if (!damageSource.isBypassArmor()) {
            damage = CombatRules.getDamageAfterAbsorb(damage, this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }
        return damage;
    }


    @Inject(method = "drop*", cancellable = true, at = @At("HEAD"))
    private void drop1(boolean dropAll, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        ItemStack dropped = inventory.getItem(inventory.selected);

        if (FfaClassicUtil.isFfaPlayer(player)) {
            cir.setReturnValue(false);
            return;
        }
         if(LobbyUtil.isLobbyWorld(player.getLevel())){
             cir.setReturnValue(false);
             return;
         }
        if (BwUtil.isBedWarsPlayer(player)) {
            if (!BwUtil.canDropItem(dropped)) {
                ItemStackUtil.sendInventoryRefreshPacket(player);
                cir.setReturnValue(false);
                return;
            }
        }

        if(OitcGame.isOITCPlayer(player)){
            cir.setReturnValue(false);
        }

    }

    @Inject(method = "getAttackDelay", at = @At("HEAD"))
    private void getAttackDelay(CallbackInfoReturnable<Integer> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        if (BwUtil.isBedWarsPlayer(player)) {
            BwUtil.setAttackSpeed(player);
        }

    }
}
