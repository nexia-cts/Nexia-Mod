package com.nexia.core.utilities.misc;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.ffa.KitGUI;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUtil {

    public static boolean dropItem(NexiaPlayer player, ItemStack itemStack) {
        //return !FfaUtil.isFfaPlayer(player) || !LobbyUtil.isLobbyWorld(player.level) || !OitcGame.isOITCPlayer(player) || !FootballGame.isFootballPlayer(player);

        if(FfaUtil.isFfaPlayer(player)) return false;
        if(LobbyUtil.isLobbyWorld(player.unwrap().level)) return false;
        if(OitcGame.isOITCPlayer(player)) return false;
        if(FootballGame.isFootballPlayer(player)) return false;

        if (BwUtil.isBedWarsPlayer(player) && !BwUtil.canDropItem(itemStack)) return false;

        return true;
    }

    public static boolean dropItem(NexiaPlayer player, com.combatreforged.factory.api.world.item.ItemStack itemStack) {

        if(FfaUtil.isFfaPlayer(player)) return false;
        if(LobbyUtil.isLobbyWorld(player.unwrap().level)) return false;
        if(OitcGame.isOITCPlayer(player)) return false;
        if(FootballGame.isFootballPlayer(player)) return false;

        if (BwUtil.isBedWarsPlayer(player) && !BwUtil.canDropItem(itemStack)) return false;

        return true;
    }

    public static void onSignClick(CallbackInfoReturnable<InteractionResult> ci, BlockPos signPos, Level level, ServerPlayer p) {
        if (level.equals(com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld)) {
            List<BlockPos> bp = new ArrayList<>(Arrays.asList(
                    new BlockPos(0, 81, 6),
                    new BlockPos(-6, 81, 0),
                    new BlockPos(0, 81, -6),
                    new BlockPos(6, 81, 0)
            ));

            if(bp.contains(signPos)) {
                KitGUI.openKitGUI(p);
            }
            ci.setReturnValue(InteractionResult.PASS);
        }

        p.swing(InteractionHand.MAIN_HAND);
    }
}
