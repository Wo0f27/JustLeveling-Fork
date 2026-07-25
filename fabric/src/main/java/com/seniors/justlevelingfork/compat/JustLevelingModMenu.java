package com.seniors.justlevelingfork.compat;

import com.seniors.justlevelingfork.client.core.SortPassives;
import com.seniors.justlevelingfork.client.core.SortSkills;
import com.seniors.justlevelingfork.client.screen.CommonConfigScreen;
import com.seniors.justlevelingfork.config.FabricCommonConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class JustLevelingModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new CommonConfigScreen(parent, new FabricConfigSaver());
    }

    private static final class FabricConfigSaver implements CommonConfigScreen.ConfigSaver {
        @Override
        public void setAptitudeMaxLevel(int value) {
            FabricCommonConfig.setAptitudeMaxLevel(value);
        }

        @Override
        public void setPlayersMaxGlobalLevel(int value) {
            FabricCommonConfig.setPlayersMaxGlobalLevel(value);
        }

        @Override
        public void setAptitudeFirstCostLevel(int value) {
            FabricCommonConfig.setAptitudeFirstCostLevel(value);
        }

        @Override
        public void setShowPotionsHud(boolean value) {
            FabricCommonConfig.setShowPotionsHud(value);
        }

        @Override
        public void setShowLuckyDropSkillOverlay(boolean value) {
            FabricCommonConfig.setShowLuckyDropSkillOverlay(value);
        }

        @Override
        public void setShowCriticalRollSkillOverlay(boolean value) {
            FabricCommonConfig.setShowCriticalRollSkillOverlay(value);
        }

        @Override
        public void setShowSkillModName(boolean value) {
            FabricCommonConfig.setShowSkillModName(value);
        }

        @Override
        public void setShowTitleModName(boolean value) {
            FabricCommonConfig.setShowTitleModName(value);
        }

        @Override
        public void setPassiveSort(SortPassives value) {
            FabricCommonConfig.setPassiveSort(value);
        }

        @Override
        public void setSkillSort(SortSkills value) {
            FabricCommonConfig.setSkillSort(value);
        }

        @Override
        public void setDropLockedItems(boolean value) {
            FabricCommonConfig.setDropLockedItems(value);
        }

        @Override
        public void setAllowLockedItemsInInventory(boolean value) {
            FabricCommonConfig.setAllowLockedItemsInInventory(value);
        }

        @Override
        public void setHideMetUsageRequirements(boolean value) {
            FabricCommonConfig.setHideMetUsageRequirements(value);
        }

        @Override
        public void setSkillResetRefundsSpentLevels(boolean value) {
            FabricCommonConfig.setSkillResetRefundsSpentLevels(value);
        }
    }
}
