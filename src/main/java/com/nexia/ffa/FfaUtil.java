package com.nexia.ffa;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class FfaUtil {

    public static final String FFA_TAG = "ffa";

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        return player.getTags().contains("ffa") || player.getTags().contains("ffa_classic") || player.getTags().contains("ffa_kits") || player.getTags().contains("ffa_sky") || player.getTags().contains("ffa_uhc") && com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.FFA;
    }

    public static float calculateHealth(float health){
        float fixedHealth = Float.parseFloat(new DecimalFormat("#.#").format(health / 2));

        if(fixedHealth <= 0){
            return 0.5f;
        }
        if(fixedHealth >= 10){
            return 10f;
        }

        if(!(fixedHealth % 1 == 0)){ return fixedHealth; }

        if(Float.parseFloat(new DecimalFormat("#.5").format(fixedHealth)) >= 10.5){
            return 10f;
        }
        if(((fixedHealth / 2) % 1) >= .5){
            return Float.parseFloat(new DecimalFormat("#.5").format(fixedHealth));
        }
        return Float.parseFloat(new DecimalFormat("#.0").format(fixedHealth));
    }

    public static void leaveOrDie(@NotNull ServerPlayer player, @Nullable DamageSource source, boolean leaving) {
        player.inventory.setCarried(ItemStack.EMPTY);
        PlayerData data = PlayerDataManager.get(player);

        if(data.ffaGameMode == FfaGameMode.CLASSIC) {
            FfaClassicUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(data.ffaGameMode == FfaGameMode.KITS) {
            FfaKitsUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(data.ffaGameMode == FfaGameMode.SKY) {
            FfaSkyUtil.wasInSpawn.remove(player.getUUID());
            FfaSkyUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(data.ffaGameMode == FfaGameMode.UHC) {
            FfaUhcUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(leaving) data.ffaGameMode = null;
    }
}
