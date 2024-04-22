package com.nexia.core.mixin.item;

import com.nexia.core.Main;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElytraItem.class)
public class ElytraItemMixin {
    @Unique
    public ItemStack copyAndEmpty(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack1 = itemStack.copy();
        itemStack.setCount(0);
        return itemStack1;
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if(!Main.config.enhancements.armorSwapping) return;

        ItemStack itemStack = player.getItemInHand(interactionHand);
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
        ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
        if (EnchantmentHelper.hasBindingCurse(itemStack2) && !player.isCreative() || ItemStack.isSame(itemStack, itemStack2)) {
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
            return;
        }

        ItemStack itemStack3 = itemStack2.isEmpty() ? itemStack : copyAndEmpty(itemStack2);
        ItemStack itemStack4 = copyAndEmpty(itemStack);
        player.setItemSlot(equipmentSlot, itemStack4);
        cir.setReturnValue(InteractionResultHolder.success(itemStack3));
    }
}
