package com.nexia.core.mixin.item;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.PlayGUI;
import com.nexia.core.gui.RankGUI;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class UseItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void changeUse(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        PlayerGameMode gameMode = PlayerDataManager.get(player).gameMode;
        if(player.getItemInHand(interactionHand).getDisplayName().getString().toLowerCase().contains("gamemode selector") && gameMode == PlayerGameMode.LOBBY) {
            PlayGUI.openMainGUI((ServerPlayer) player);
        }
        if(player.getItemInHand(interactionHand).getDisplayName().getString().toLowerCase().contains("prefix selector") && gameMode == PlayerGameMode.LOBBY) {
            RankGUI.openRankGUI((ServerPlayer) player);
        }
    }
}
