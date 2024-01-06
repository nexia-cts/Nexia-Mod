package com.nexia.core.mixin.item;

import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
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

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof ServerPlayer) {
            this.player = (ServerPlayer) livingEntity;
        }
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void finishedUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (this.player == null) return;

        if (BwUtil.isBedWarsPlayer(this.player)) {
            BwPlayerEvents.drankPotion(this.player, itemStack);
        }
    }

    @Redirect(method = "finishUsingItem", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack setItemAfterDrink(ItemLike itemLike) {
        if (player == null) return new ItemStack(Items.GLASS_BOTTLE);

        if (BwUtil.isBedWarsPlayer(player)) {
            ItemStackUtil.sendInventoryRefreshPacket(player);
            return ItemStack.EMPTY;
        }

        return new ItemStack(Items.GLASS_BOTTLE);
    }

}
