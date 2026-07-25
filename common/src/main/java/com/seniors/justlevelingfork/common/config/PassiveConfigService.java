package com.seniors.justlevelingfork.common.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class PassiveConfigService {
    private static Supplier<Map<String, PassiveConfig>> passiveConfigs = PassiveConfigService::defaultConfigs;

    private PassiveConfigService() {
    }

    public static void setPassiveConfigs(Supplier<Map<String, PassiveConfig>> passiveConfigs) {
        PassiveConfigService.passiveConfigs =
                Optional.ofNullable(passiveConfigs).orElse(PassiveConfigService::defaultConfigs);
    }

    public static Map<String, PassiveConfig> passiveConfigs() {
        Map<String, PassiveConfig> configs = passiveConfigs.get();
        return configs == null || configs.isEmpty() ? defaultConfigs() : sanitize(configs);
    }

    public static PassiveConfig passiveConfig(String passiveName) {
        return passiveConfigs().getOrDefault(passiveName, defaultConfigs().get(passiveName));
    }

    public static double value(String passiveName, double defaultValue) {
        PassiveConfig config = passiveConfig(passiveName);
        return config == null ? defaultValue : config.value();
    }

    public static int[] levels(String passiveName, int[] defaultLevels) {
        PassiveConfig config = passiveConfig(passiveName);
        return config == null ? Arrays.copyOf(defaultLevels, defaultLevels.length) : config.levels();
    }

    public static Map<String, PassiveConfig> defaultConfigs() {
        Map<String, PassiveConfig> configs = new LinkedHashMap<>();
        configs.put("attack_damage", new PassiveConfig(1.5D, levels10()));
        configs.put("attack_knockback", new PassiveConfig(0.4D, levels5()));
        configs.put("max_health", new PassiveConfig(20.0D, levels10()));
        configs.put("knockback_resistance", new PassiveConfig(0.5D, levels5()));
        configs.put("movement_speed", new PassiveConfig(0.05D, levels5()));
        configs.put("projectile_damage", new PassiveConfig(5.0D, levels5()));
        configs.put("armor", new PassiveConfig(4.0D, levels5()));
        configs.put("armor_toughness", new PassiveConfig(1.0D, levels5()));
        configs.put("attack_speed", new PassiveConfig(0.4D, levels5()));
        configs.put("entity_reach", new PassiveConfig(1.0D, levels5()));
        configs.put("block_reach", new PassiveConfig(1.5D, levels5()));
        configs.put("break_speed", new PassiveConfig(0.5D, new int[] {8, 14, 20, 26, 32}));
        configs.put("beneficial_effect", new PassiveConfig(60.0D, levels10()));
        configs.put("magic_resist", new PassiveConfig(0.5D, levels5()));
        configs.put("critical_damage", new PassiveConfig(0.25D, levels10()));
        configs.put("luck", new PassiveConfig(2.0D, levels10()));
        return configs;
    }

    public static Map<String, PassiveConfig> sanitize(Map<String, PassiveConfig> configs) {
        Map<String, PassiveConfig> sanitized = new LinkedHashMap<>(defaultConfigs());
        configs.forEach((name, config) -> {
            if (name != null && config != null) {
                sanitized.put(name, config.sanitized(sanitized.get(name)));
            }
        });
        return sanitized;
    }

    private static int[] levels5() {
        return new int[] {8, 14, 20, 26, 32};
    }

    private static int[] levels10() {
        return new int[] {5, 8, 11, 14, 17, 20, 23, 26, 29, 32};
    }

    public record PassiveConfig(double value, int[] levels) {
        public PassiveConfig sanitized(PassiveConfig fallback) {
            double sanitizedValue = Math.max(0.0D, value);
            int[] sanitizedLevels = levels == null || levels.length == 0
                    ? fallback == null ? new int[] {8, 14, 20, 26, 32} : fallback.levels()
                    : Arrays.stream(levels).map(level -> Math.max(1, level)).toArray();
            return new PassiveConfig(sanitizedValue, sanitizedLevels);
        }

        @Override
        public int[] levels() {
            return Arrays.copyOf(levels, levels.length);
        }
    }
}
