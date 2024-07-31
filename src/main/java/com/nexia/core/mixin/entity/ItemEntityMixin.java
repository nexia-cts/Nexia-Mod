package com.nexia.core.mixin.entity;

import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(at = @At("TAIL"), method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V")
    private static void mergeTail(ItemEntity itemEntity, ItemStack itemStack, ItemStack itemStack2, CallbackInfo ci) {

        if (BwAreas.isBedWarsWorld(itemEntity.level)) {
            BwUtil.afterItemMerge(itemEntity);
        }

    }

}
