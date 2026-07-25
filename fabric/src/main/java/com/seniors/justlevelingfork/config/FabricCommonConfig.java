package com.seniors.justlevelingfork.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.client.core.SortPassives;
import com.seniors.justlevelingfork.client.core.SortSkills;
import com.seniors.justlevelingfork.common.config.ClientConfigService;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricCommonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FabricCommonConfigValues values = new FabricCommonConfigValues();
    private static Path configPath;

    private FabricCommonConfig() {
    }

    public static void load() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(Constants.CONFIG_NAME + ".json");
        values = readOrCreate(configPath);
        CommonConfigService.setAptitudeMaxLevel(() -> values.aptitudeMaxLevel);
        CommonConfigService.setPlayersMaxGlobalLevel(() -> values.playersMaxGlobalLevel);
        CommonConfigService.setAptitudeFirstCostLevel(() -> values.aptitudeFirstCostLevel);
        CommonConfigService.setShowPotionsHud(() -> values.showPotionsHud);
        ClientConfigService.setShowLuckyDropSkillOverlay(() -> values.showLuckyDropSkillOverlay);
        ClientConfigService.setShowCriticalRollSkillOverlay(() -> values.showCriticalRollSkillOverlay);
        ClientConfigService.setShowSkillModName(() -> values.showSkillModName);
        ClientConfigService.setShowTitleModName(() -> values.showTitleModName);
        ClientConfigService.setPassiveSort(() -> values.sortPassive);
        ClientConfigService.setSkillSort(() -> values.sortSkill);
        CommonConfigService.setDropLockedItems(() -> values.dropLockedItems);
        CommonConfigService.setHideMetUsageRequirements(() -> values.hideMetUsageRequirements);
        CommonConfigService.setSkillResetRefundsSpentLevels(() -> values.skillResetRefundsSpentLevels);
        CommonConfigService.setTreasureHunterItems(() -> values.treasureHunterItemList);
        CommonConfigService.setConvergenceItems(() -> values.convergenceItemList);
    }

    public static void setAptitudeMaxLevel(int level) {
        values.aptitudeMaxLevel = Math.max(2, level);
        save();
    }

    public static void setPlayersMaxGlobalLevel(int level) {
        values.playersMaxGlobalLevel = Math.max(32, level);
        save();
    }

    public static void setAptitudeFirstCostLevel(int level) {
        values.aptitudeFirstCostLevel = Math.max(1, level);
        save();
    }

    public static void setShowPotionsHud(boolean value) {
        values.showPotionsHud = value;
        save();
    }

    public static void setShowLuckyDropSkillOverlay(boolean value) {
        values.showLuckyDropSkillOverlay = value;
        save();
    }

    public static void setShowCriticalRollSkillOverlay(boolean value) {
        values.showCriticalRollSkillOverlay = value;
        save();
    }

    public static void setShowSkillModName(boolean value) {
        values.showSkillModName = value;
        save();
    }

    public static void setShowTitleModName(boolean value) {
        values.showTitleModName = value;
        save();
    }

    public static void setPassiveSort(SortPassives value) {
        values.sortPassive = value == null ? SortPassives.ByName : value;
        save();
    }

    public static void setSkillSort(SortSkills value) {
        values.sortSkill = value == null ? SortSkills.ByLevel : value;
        save();
    }

    public static void setDropLockedItems(boolean value) {
        values.dropLockedItems = value;
        save();
    }

    public static void setHideMetUsageRequirements(boolean value) {
        values.hideMetUsageRequirements = value;
        save();
    }

    public static void setSkillResetRefundsSpentLevels(boolean value) {
        values.skillResetRefundsSpentLevels = value;
        save();
    }

    private static FabricCommonConfigValues readOrCreate(Path path) {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                FabricCommonConfigValues loaded = GSON.fromJson(reader, FabricCommonConfigValues.class);
                return loaded == null ? new FabricCommonConfigValues() : loaded.sanitized();
            } catch (IOException exception) {
                Constants.LOG.warn("Failed to read Fabric config {}, using defaults", path, exception);
                return new FabricCommonConfigValues();
            }
        }

        FabricCommonConfigValues created = new FabricCommonConfigValues();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(created, writer);
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to create Fabric config {}, using defaults", path, exception);
        }
        return created;
    }

    private static void save() {
        if (configPath == null) {
            return;
        }

        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(values, writer);
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to save Fabric config {}", configPath, exception);
        }
    }

    private static final class FabricCommonConfigValues {
        private int aptitudeMaxLevel = 32;
        private int playersMaxGlobalLevel = 256;
        private int aptitudeFirstCostLevel = 5;
        private boolean showPotionsHud = true;
        private boolean showLuckyDropSkillOverlay = true;
        private boolean showCriticalRollSkillOverlay = true;
        private boolean showSkillModName = false;
        private boolean showTitleModName = false;
        private SortPassives sortPassive = SortPassives.ByName;
        private SortSkills sortSkill = SortSkills.ByLevel;
        private boolean dropLockedItems = false;
        private boolean hideMetUsageRequirements = false;
        private boolean skillResetRefundsSpentLevels = false;
        private List<String> treasureHunterItemList = new ArrayList<>(CommonConfigService.defaultTreasureHunterItems());
        private List<String> convergenceItemList = new ArrayList<>(CommonConfigService.defaultConvergenceItems());

        private FabricCommonConfigValues sanitized() {
            aptitudeMaxLevel = Math.max(2, aptitudeMaxLevel);
            playersMaxGlobalLevel = Math.max(32, playersMaxGlobalLevel);
            aptitudeFirstCostLevel = Math.max(1, aptitudeFirstCostLevel);
            if (sortPassive == null) {
                sortPassive = SortPassives.ByName;
            }
            if (sortSkill == null) {
                sortSkill = SortSkills.ByLevel;
            }
            if (treasureHunterItemList == null) {
                treasureHunterItemList = new ArrayList<>(CommonConfigService.defaultTreasureHunterItems());
            }
            if (convergenceItemList == null) {
                convergenceItemList = new ArrayList<>(CommonConfigService.defaultConvergenceItems());
            }
            return this;
        }
    }
}
