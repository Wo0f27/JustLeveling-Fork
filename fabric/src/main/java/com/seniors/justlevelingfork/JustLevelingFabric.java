package com.seniors.justlevelingfork;

import com.seniors.justlevelingfork.config.FabricCommonConfig;
import com.seniors.justlevelingfork.config.FabricLockItemStore;
import com.seniors.justlevelingfork.config.FabricPassiveConfigStore;
import com.seniors.justlevelingfork.config.FabricSkillConfigStore;
import com.seniors.justlevelingfork.config.FabricTitleModelStore;
import com.seniors.justlevelingfork.common.player.FabricPlayerProgressStore;
import com.seniors.justlevelingfork.integration.FabricKubeJSIntegration;
import com.seniors.justlevelingfork.integration.ftbquests.FTBQuestsIntegration;
import com.seniors.justlevelingfork.integration.questlog.QuestlogIntegration;
import com.seniors.justlevelingfork.network.FabricServerNetworking;
import com.seniors.justlevelingfork.registry.FabricRegistrySounds;
import com.seniors.justlevelingfork.registry.FabricRegistryAttributes;
import com.seniors.justlevelingfork.registry.FabricRegistryAptitudes;
import com.seniors.justlevelingfork.registry.FabricRegistryCommonEvents;
import com.seniors.justlevelingfork.registry.FabricRegistryItems;
import com.seniors.justlevelingfork.registry.FabricRegistryPassives;
import com.seniors.justlevelingfork.registry.FabricRegistrySkills;
import com.seniors.justlevelingfork.registry.FabricRegistryTitles;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class JustLevelingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        JustLevelingCommon.init();
        FabricCommonConfig.load();
        FabricPassiveConfigStore.load();
        FabricSkillConfigStore.load();
        FabricTitleModelStore.load();
        FabricLockItemStore.load();
        FabricPlayerProgressStore.load();
        FabricRegistryAptitudes.load();
        FabricRegistryPassives.load();
        FabricRegistrySkills.load();
        FabricRegistryTitles.load();
        FabricRegistryItems.load();
        FabricRegistrySounds.load();
        FabricRegistryAttributes.load();
        if (FabricLoader.getInstance().isModLoaded("kubejs")) {
            FabricKubeJSIntegration.load();
        }
        if (FabricLoader.getInstance().isModLoaded("ftbquests")) {
            FTBQuestsIntegration.load();
        }
        if (FabricLoader.getInstance().isModLoaded("questlog")) {
            QuestlogIntegration.load();
        }
        FabricRegistryCommonEvents.load();
        FabricServerNetworking.init();
    }
}
