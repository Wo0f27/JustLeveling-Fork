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
                buffer.readList(FriendlyByteBuf::readUtf),
                buffer.readList(FriendlyByteBuf::readUtf),
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
        Map<String, PassiveConfig> configs = new java.util.LinkedHashMap<>();
        for (int index = 0; index < size; index++) {
            String name = buffer.readUtf();
            double value = buffer.readDouble();
            int levelCount = buffer.readInt();
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
        Map<String, SkillConfig> configs = new java.util.LinkedHashMap<>();
        for (int index = 0; index < size; index++) {
            String name = buffer.readUtf();
            int requiredLevel = buffer.readInt();
            int valueCount = buffer.readInt();
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
}
