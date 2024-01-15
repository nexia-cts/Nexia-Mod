package com.nexia.core.mixin.item;

import com.nexia.minigames.games.bedwars.areas.BwAreas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompassItem.class)
public class CompassItemMixin {

    @Unique
    Level level;
    @Inject(at = @At("HEAD"), method = "inventoryTick")
    private void tick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl, CallbackInfo ci) {
        this.level = level;
    }

    // Not require a lodestone block in order to track someone in oldbedwars
    @Redirect(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;remove(Ljava/lang/String;)V"))
    private void existsAtPosition(CompoundTag instance, String string) {

        if (BwAreas.isBedWarsWorld(level)) return;
        instance.remove(string);

    }

}
