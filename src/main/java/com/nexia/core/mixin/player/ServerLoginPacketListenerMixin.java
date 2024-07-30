package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.time.ServerType;
import com.nexia.discord.NexiaDiscord;
import com.nexia.discord.commands.discord.LinkSlashCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
        if ((!(original instanceof TranslatableComponent component))) return original;

        if (component.getKey().contains("banned")) {
            component = new TranslatableComponent("§c§lYou have been banned.\n§7Reason: §d" + component.getString().split("Reason: ")[1] + "\n§7You can appeal your ban at §d" + NexiaDiscord.config.discordLink);
        }

        if(!PlayerDataManager.getDataManager(NexiaCore.DISCORD_DATA_MANAGER).get(gameProfile.getId()).savedData.get(Boolean.class, "isLinked") && ServerTime.serverType.equals(ServerType.DEV)) {
            int id = RandomUtil.randomInt(1000, 9999);

            if (LinkSlashCommand.idMinecraft.containsKey(id)) {
                id = RandomUtil.randomInt(1000, 9999);
            }

            LinkSlashCommand.idMinecraft.put(id, gameProfile.getId());
            component = new TranslatableComponent("§c§lYou may have joined the wrong server.\n\n§5§lEU§7: eu.nexia.dev\n§5§lNA§7: na.nexia.dev\n\n§7If you were intending to join the development server then you can link your account here:\n§7Code: §5§l" + id);
        }

        return component;
    }

}
