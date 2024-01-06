package com.nexia.core.mixin.item;

import com.nexia.core.Main;
import net.minecraft.world.item.EnderpearlItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnderpearlItem.class)
public class EnderPearlItemMixin {
    @ModifyArg(method = "use", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"))
    private int setPearlCooldown(int original) {
        return Main.config.enhancements.enderpearlCooldown;
    }
}
