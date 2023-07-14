package com.nexia.core.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import com.nexia.discord.utilities.player.PlayerData;
import com.nexia.discord.utilities.player.PlayerDataManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.nexia.discord.Main.jda;

@Mixin(DedicatedPlayerList.class)
public abstract class DedicatedPlayerListMixin {
    /**
     * @author NotCoded
     * @reason Allow supporter ranks to join the server
     */

    @ModifyReturnValue(method = "isWhiteListed", at = @At("RETURN"))
    public boolean allowSupporters(boolean original, GameProfile gameProfile) {
        PlayerData playerData = PlayerDataManager.get(gameProfile.getId());

        Member member;
        Role role = jda.getRoleById("1125391407616630845"); // Supporter++

        try {
            member = jda.getGuildById(com.nexia.discord.Main.config.guildID).retrieveMemberById(playerData.savedData.discordID).complete(true);
        } catch (Exception ignored) { return original; }

        return playerData.savedData.isLinked && member.getRoles().contains(role);
    }

}
