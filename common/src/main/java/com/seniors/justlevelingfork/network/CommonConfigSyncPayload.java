package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.config.PassiveConfigService;
import com.seniors.justlevelingfork.common.config.PassiveConfigService.PassiveConfig;
import com.seniors.justlevelingfork.common.config.SkillConfigService;
import com.seniors.justlevelingfork.common.config.SkillConfigService.SkillConfig;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;

public record CommonConfigSyncPayload(
        int aptitudeMaxLevel,
        int playersMaxGlobalLevel,
        int aptitudeFirstCostLevel,
        boolean showPotionsHud,
        boolean dropLockedItems,
        boolean hideMetUsageRequirements,
        boolean skillResetRefundsSpentLevels,
        List<String> treasureHunterItems,
        List<String> convergenceItems,
        Map<String, PassiveConfig> passiveConfigs,
        Map<String, SkillConfig> skillConfigs) {
    private static final int MAX_ITEM_ENTRIES = 4096;
    private static final int MAX_CONFIG_ENTRIES = 256;
    private static final int MAX_LEVELS_PER_PASSIVE = 256;
    private static final int MAX_VALUES_PER_SKILL = 64;
    private static final int MAX_ENTRY_LENGTH = 8192;
    public static CommonConfigSyncPayload current() {
        return new CommonConfigSyncPayload(
                CommonConfigService.aptitudeMaxLevel(),
                CommonConfigService.playersMaxGlobalLevel(),
                CommonConfigService.aptitudeFirstCostLevel(),
                CommonConfigService.showPotionsHud(),
                CommonConfigService.dropLockedItems(),
                CommonConfigService.hideMetUsageRequirements(),
                CommonConfigService.skillResetRefundsSpentLevels(),
                List.copyOf(CommonConfigService.treasureHunterItems()),
                List.copyOf(CommonConfigService.convergenceItems()),
                PassiveConfigService.passiveConfigs(),
                SkillConfigService.skillConfigs());
    }

    public static CommonConfigSyncPayload read(FriendlyByteBuf buffer) {
        return new CommonConfigSyncPayload(
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                readStringList(buffer, "treasure hunter items"),
                readStringList(buffer, "convergence items"),
                readPassiveConfigs(buffer),
                readSkillConfigs(buffer));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.aptitudeMaxLevel);
        buffer.writeInt(this.playersMaxGlobalLevel);
        buffer.writeInt(this.aptitudeFirstCostLevel);
        buffer.writeBoolean(this.showPotionsHud);
        buffer.writeBoolean(this.dropLockedItems);
        buffer.writeBoolean(this.hideMetUsageRequirements);
        buffer.writeBoolean(this.skillResetRefundsSpentLevels);
        buffer.writeCollection(this.treasureHunterItems, FriendlyByteBuf::writeUtf);
        buffer.writeCollection(this.convergenceItems, FriendlyByteBuf::writeUtf);
        writePassiveConfigs(buffer, this.passiveConfigs);
        writeSkillConfigs(buffer, this.skillConfigs);
    }

    public void apply() {
        CommonConfigService.setAptitudeMaxLevel(() -> this.aptitudeMaxLevel);
        CommonConfigService.setPlayersMaxGlobalLevel(() -> this.playersMaxGlobalLevel);
        CommonConfigService.setAptitudeFirstCostLevel(() -> this.aptitudeFirstCostLevel);
        CommonConfigService.setShowPotionsHud(() -> this.showPotionsHud);
        CommonConfigService.setDropLockedItems(() -> this.dropLockedItems);
        CommonConfigService.setHideMetUsageRequirements(() -> this.hideMetUsageRequirements);
        CommonConfigService.setSkillResetRefundsSpentLevels(() -> this.skillResetRefundsSpentLevels);
        CommonConfigService.setTreasureHunterItems(() -> this.treasureHunterItems);
        CommonConfigService.setConvergenceItems(() -> this.convergenceItems);
        PassiveConfigService.setPassiveConfigs(() -> this.passiveConfigs);
        SkillConfigService.setSkillConfigs(() -> this.skillConfigs);
    }

    private static Map<String, PassiveConfig> readPassiveConfigs(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        requireCount(size, MAX_CONFIG_ENTRIES, "passive configs");
        Map<String, PassiveConfig> configs = new java.util.LinkedHashMap<>();
        for (int index = 0; index < size; index++) {
            String name = buffer.readUtf(MAX_ENTRY_LENGTH);
            double value = buffer.readDouble();
            int levelCount = buffer.readInt();
            requireCount(levelCount, MAX_LEVELS_PER_PASSIVE, "passive levels");
            int[] levels = new int[levelCount];
            for (int levelIndex = 0; levelIndex < levelCount; levelIndex++) {
                levels[levelIndex] = buffer.readInt();
            }
            configs.put(name, new PassiveConfig(value, levels));
        }
        return PassiveConfigService.sanitize(configs);
    }

    private static void writePassiveConfigs(FriendlyByteBuf buffer, Map<String, PassiveConfig> configs) {
        Map<String, PassiveConfig> sanitized = PassiveConfigService.sanitize(configs);
        buffer.writeInt(sanitized.size());
        sanitized.forEach((name, config) -> {
            buffer.writeUtf(name);
            buffer.writeDouble(config.value());
            int[] levels = config.levels();
            buffer.writeInt(levels.length);
            for (int level : levels) {
                buffer.writeInt(level);
            }
        });
    }

    private static Map<String, SkillConfig> readSkillConfigs(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        requireCount(size, MAX_CONFIG_ENTRIES, "skill configs");
        Map<String, SkillConfig> configs = new java.util.LinkedHashMap<>();
        for (int index = 0; index < size; index++) {
            String name = buffer.readUtf(MAX_ENTRY_LENGTH);
            int requiredLevel = buffer.readInt();
            int valueCount = buffer.readInt();
            requireCount(valueCount, MAX_VALUES_PER_SKILL, "skill values");
            double[] values = new double[valueCount];
            for (int valueIndex = 0; valueIndex < valueCount; valueIndex++) {
                values[valueIndex] = buffer.readDouble();
            }
            configs.put(name, new SkillConfig(requiredLevel, values));
        }
        return SkillConfigService.sanitize(configs);
    }

    private static void writeSkillConfigs(FriendlyByteBuf buffer, Map<String, SkillConfig> configs) {
        Map<String, SkillConfig> sanitized = SkillConfigService.sanitize(configs);
        buffer.writeInt(sanitized.size());
        sanitized.forEach((name, config) -> {
            buffer.writeUtf(name);
            buffer.writeInt(config.requiredLevel());
            double[] values = config.values();
            buffer.writeInt(values.length);
            for (double value : values) {
                buffer.writeDouble(value);
            }
        });
    }

    private static List<String> readStringList(FriendlyByteBuf buffer, String description) {
        int size = buffer.readVarInt();
        requireCount(size, MAX_ITEM_ENTRIES, description);
        List<String> entries = new java.util.ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            entries.add(buffer.readUtf(MAX_ENTRY_LENGTH));
        }
        return entries;
    }

    private static void requireCount(int count, int maximum, String description) {
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("Invalid " + description + " count: " + count);
        }
    }
}
