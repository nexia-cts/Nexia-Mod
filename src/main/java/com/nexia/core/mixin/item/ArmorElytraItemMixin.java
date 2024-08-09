package com.nexia.core.mixin.item;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {ElytraItem.class, ArmorItem.class})
public class ArmorElytraItemMixin {
    @Unique
    public ItemStack copyAndEmpty(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack1 = itemStack.copy();
        itemStack.setCount(0);
        return itemStack1;
    }

    @Unique
    private void playEquipSoundForClient(ServerPlayer player, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            SoundEvent soundEvent = SoundEvents.ARMOR_EQUIP_GENERIC;
            Item item = itemStack.getItem();
            if (item instanceof ArmorItem) {
                soundEvent = ((ArmorItem)item).getMaterial().getEquipSound();
            } else if (item == Items.ELYTRA) {
                soundEvent = SoundEvents.ARMOR_EQUIP_ELYTRA;
            }

            player.connection.send(new ClientboundSoundPacket(soundEvent, player.getSoundSource(),
                    player.position().x, player.position().y, player.position().z, 16f, 1.0F));
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
        ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
        if ((EnchantmentHelper.hasBindingCurse(itemStack2) && !player.isCreative()) || ItemStack.matches(itemStack, itemStack2)) {
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
            return;
        }

        ItemStack itemStack3 = itemStack2.isEmpty() ? itemStack : copyAndEmpty(itemStack2);
        ItemStack itemStack4 = player.isCreative() ? itemStack.copy() : copyAndEmpty(itemStack);
        player.setItemSlot(equipmentSlot, itemStack4);
        playEquipSoundForClient((ServerPlayer) player, itemStack4);
        cir.setReturnValue(InteractionResultHolder.success(itemStack3));
    }
}
