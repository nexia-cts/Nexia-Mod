package com.nexia.core.mixin.misc;

import com.nexia.core.utilities.time.ServerTime;
import net.notcoded.codelib.util.server.ServerUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerUtils.class)
public class ServerUtilsMixin {

    /**
     * @author NotCoded
     * @reason Use Factory server run command
     */
    @Overwrite
    public static void runCommand(String command) {
        ServerTime.factoryServer.runCommand(command, 4, false);
    }
}
