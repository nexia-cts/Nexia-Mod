package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.Discord;
import com.nexia.discord.Main;
import com.nexia.discord.utilities.player.PlayerDataManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerMixin {

    @Shadow private GameProfile gameProfile;

    @ModifyArg(method = "handleAcceptedLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private Component handleAcceptedLogin(Component original) {
        if (!(original instanceof TranslatableComponent)) return original;
        TranslatableComponent component = (TranslatableComponent) original;

        if (component.getKey().contains("banned")) {
            component = new TranslatableComponent("§c§lYou have been banned.\n§7Reason: §d" + component.getString().split("Reason: ")[1] + "\n§7You can appeal your ban at §d" + Main.config.discordLink);
        }

        if(!PlayerDataManager.get(gameProfile.getId()).savedData.isLinked) {
            int id = RandomUtil.randomInt(1000, 9999);

            if (Discord.idMinecraft.containsKey(id)) {
                id = RandomUtil.randomInt(1000, 9999);
            }

            Discord.idMinecraft.put(id, gameProfile.getId());
            component = new TranslatableComponent("§cYou are not whitelisted!\n§7If you have §5Supporter§5§l++ §7rank but aren't linked, then you can link your account here:\n§7Code: §5§l" + id);
        }

        return component;
    }

}
