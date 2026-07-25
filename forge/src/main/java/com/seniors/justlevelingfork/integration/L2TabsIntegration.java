package com.seniors.justlevelingfork.integration;

import net.minecraftforge.fml.ModList;

public final class L2TabsIntegration {
    private L2TabsIntegration() {
    }

    public static boolean isModLoaded() {
        return ModList.get().isLoaded("l2tabs");
    }
}
