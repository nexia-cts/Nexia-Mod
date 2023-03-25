package com.nexia.core.mixin.player;

import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerMixin {

    @ModifyArg(method = "handleAcceptedLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private Component handleAcceptedLogin(Component original) {
        if (!(original instanceof TranslatableComponent)) return original;
        TranslatableComponent component = (TranslatableComponent) original;

        if (component.getKey().contains("banned")) {
            component = new TranslatableComponent("§c§lYou have been banned.\n§7Reason: §d" + component.getString().split("Reason: ")[1] + "\n§7You can appeal your ban at §d" + Main.config.discordLink);
        }

        return component;
    }

}
