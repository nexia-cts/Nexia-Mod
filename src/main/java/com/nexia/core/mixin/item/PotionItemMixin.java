package com.nexia.core.mixin.item;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Unique
    private ServerPlayer player = null;

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof ServerPlayer) {
            this.player = (ServerPlayer) livingEntity;
            NexiaPlayer nexiaPlayer = new NexiaPlayer(this.player);

            if((FfaKitsUtil.isFfaPlayer(nexiaPlayer) && FfaKitsUtil.wasInSpawn.contains(player.getUUID())) || (FfaSkyUtil.isFfaPlayer(nexiaPlayer) && FfaSkyUtil.wasInSpawn.contains(player.getUUID())) ||(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(PlayerGameMode.LOBBY) && ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(DuelGameMode.LOBBY))) {
                cir.setReturnValue(itemStack);
                nexiaPlayer.refreshInventory();
            }
        }



    }
    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void finishedUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (this.player == null) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(this.player);

        if (BwUtil.isBedWarsPlayer(nexiaPlayer)) {
            BwPlayerEvents.drankPotion(nexiaPlayer, itemStack);
        }
    }

    @Redirect(method = "finishUsingItem", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack setItemAfterDrink(ItemLike itemLike) {
        if (player == null) return new ItemStack(Items.GLASS_BOTTLE);
        NexiaPlayer nexiaPlayer = new NexiaPlayer(this.player);

        if (BwUtil.isBedWarsPlayer(nexiaPlayer) || FfaSkyUtil.isFfaPlayer(nexiaPlayer)) {
            nexiaPlayer.refreshInventory();
            return ItemStack.EMPTY;
        }

        return new ItemStack(Items.GLASS_BOTTLE);
    }

}
