package com.nexia.core.mixin.item;

import com.nexia.core.Main;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.ffa.FfaGameMode;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderpearlItem.class)
public class EnderPearlItemMixin extends Item {

    @Unique
    private static ServerPlayer thrower;

    public EnderPearlItemMixin(Item.Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At(value = "HEAD"))
    private void setPlayer(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (player instanceof ServerPlayer) {
            thrower = (ServerPlayer) player;
        }
    }

    @ModifyArg(method = "use", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"))
    private int setPearlCooldown(int original) {
        int time = Main.config.enhancements.enderpearlCooldown;
        if (thrower == null) return time;

        DuelGameMode duelGameMode = PlayerDataManager.get(thrower).gameMode;
        FfaGameMode ffaGameMode = com.nexia.core.utilities.player.PlayerDataManager.get(thrower).ffaGameMode;
        // Check if ffa gamemode is pot.

        if(duelGameMode.equals(DuelGameMode.POT) || duelGameMode.equals(DuelGameMode.NETH_POT)) time = 300;

        return time;
    }
}
