package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.common.integration.AptitudeLevelUpEvents;
import com.seniors.justlevelingfork.kubejs.events.CustomEvents;
import com.seniors.justlevelingfork.kubejs.events.LevelUpEvent;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.server.level.ServerPlayer;

public final class FabricKubeJSIntegration {
    private FabricKubeJSIntegration() {
    }

    public static void load() {
        AptitudeLevelUpEvents.setCancellationHook(FabricKubeJSIntegration::postLevelUpEvent);
    }

    private static boolean postLevelUpEvent(ServerPlayer player, Aptitude aptitude) {
        LevelUpEvent event = new LevelUpEvent(player, aptitude);
        CustomEvents.APTITUDE_LEVELUP.post(event);
        return event.getCancelled();
    }
}
