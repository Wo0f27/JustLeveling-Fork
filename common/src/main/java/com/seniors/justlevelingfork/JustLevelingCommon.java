package com.seniors.justlevelingfork;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.handler.HandlerConditions;
import com.seniors.justlevelingfork.registry.RegistryTitles;

public final class JustLevelingCommon {
    private JustLevelingCommon() {
    }

    public static void init() {
        Constants.LOG.info("Initializing {}", Constants.MOD_NAME);
        HandlerConditions.registerDefaults(PlayerProgressService.aptitudeLevelProvider());
        RegistryTitles.defaults();
    }
}
