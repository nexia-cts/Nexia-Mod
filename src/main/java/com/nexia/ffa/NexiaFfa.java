package com.nexia.ffa;

import com.nexia.core.NexiaCore;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class NexiaFfa implements ModInitializer {

    public static final ResourceLocation FFA_CLASSIC_DATA_MANAGER = NexiaCore.id("ffa_classic");
    public static final ResourceLocation FFA_KITS_DATA_MANAGER = NexiaCore.id("ffa_kits");
    public static final ResourceLocation FFA_UHC_DATA_MANAGER = NexiaCore.id("ffa_uhc");
    public static final ResourceLocation FFA_SKY_DATA_MANAGER = NexiaCore.id("ffa_sky");

    @Override
	public void onInitialize() {
	}
}
