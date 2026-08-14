package com.seniors.justlevelingfork.common.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class SkillConfigService {
    private static Supplier<Map<String, SkillConfig>> skillConfigs = SkillConfigService::defaultConfigs;

    private SkillConfigService() {
    }

    public static void setSkillConfigs(Supplier<Map<String, SkillConfig>> skillConfigs) {
        SkillConfigService.skillConfigs = Optional.ofNullable(skillConfigs).orElse(SkillConfigService::defaultConfigs);
    }

    public static Map<String, SkillConfig> skillConfigs() {
        Map<String, SkillConfig> configs = skillConfigs.get();
        return configs == null || configs.isEmpty() ? defaultConfigs() : sanitize(configs);
    }

    public static int requiredLevel(String skillName, int defaultRequiredLevel) {
        SkillConfig config = skillConfigs().get(skillName);
        return config == null ? defaultRequiredLevel : config.requiredLevel();
    }

    public static double[] values(String skillName, double[] defaultValues) {
        SkillConfig config = skillConfigs().get(skillName);
        return config == null ? Arrays.copyOf(defaultValues, defaultValues.length) : config.values(defaultValues.length);
    }

    public static Map<String, SkillConfig> defaultConfigs() {
        Map<String, SkillConfig> configs = new LinkedHashMap<>();
        configs.put("one_handed", new SkillConfig(10, new double[] {0.5D}));
        configs.put("fighting_spirit", new SkillConfig(16, new double[] {1.0D, 3.0D}));
        configs.put("berserker", new SkillConfig(30, new double[] {30.0D}));
        configs.put("athletics", new SkillConfig(10, new double[] {1.5D}));
        configs.put("turtle_shield", new SkillConfig(20, new double[0]));
        configs.put("lion_heart", new SkillConfig(30, new double[] {50.0D}));
        configs.put("quick_reposition", new SkillConfig(10, new double[] {2.0D, 3.0D}));
        configs.put("stealth_mastery", new SkillConfig(16, new double[] {20.0D, 60.0D, 1.25D}));
        configs.put("cat_eyes", new SkillConfig(30, new double[0]));
        configs.put("snow_walker", new SkillConfig(10, new double[0]));
        configs.put("counter_attack", new SkillConfig(18, new double[] {3.0D, 50.0D}));
        configs.put("diamond_skin", new SkillConfig(30, new double[] {2.0D, 2.0D}));
        configs.put("scholar", new SkillConfig(8, new double[0]));
        configs.put("haggler", new SkillConfig(16, new double[] {20.0D}));
        configs.put("alchemy_manipulation", new SkillConfig(30, new double[] {1.0D}));
        configs.put("obsidian_smasher", new SkillConfig(12, new double[] {10.0D}));
        configs.put("treasure_hunter", new SkillConfig(20, new double[] {500.0D}));
        configs.put("convergence", new SkillConfig(30, new double[] {8.0D}));
        configs.put("safe_port", new SkillConfig(12, new double[0]));
        configs.put("life_eater", new SkillConfig(18, new double[] {1.0D}));
        configs.put("wormhole_storage", new SkillConfig(30, new double[0]));
        configs.put("critical_roll", new SkillConfig(12, new double[] {1.25D, 3.0D}));
        configs.put("lucky_drop", new SkillConfig(22, new double[] {10.0D, 2.0D}));
        configs.put("limit_breaker", new SkillConfig(30, new double[] {100.0D, 999.0D}));
        return configs;
    }

    public static Map<String, SkillConfig> sanitize(Map<String, SkillConfig> configs) {
        Map<String, SkillConfig> defaults = defaultConfigs();
        Map<String, SkillConfig> sanitized = new LinkedHashMap<>(defaults);
        configs.forEach((name, config) -> {
            SkillConfig fallback = defaults.get(name);
            if (fallback != null && config != null) {
                sanitized.put(name, config.sanitized(fallback));
            }
        });
        return sanitized;
    }

    public record SkillConfig(int requiredLevel, double[] values) {
        public SkillConfig sanitized(SkillConfig fallback) {
            int sanitizedRequiredLevel = Math.min(
                    CommonConfigService.MAX_APTITUDE_LEVEL, Math.max(0, requiredLevel));
            double[] sanitizedValues = values == null
                    ? fallback == null ? new double[0] : fallback.values()
                    : Arrays.stream(values)
                            .limit(fallback == null ? 0 : fallback.values().length)
                            .map(value -> Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D)
                            .toArray();
            return new SkillConfig(sanitizedRequiredLevel, sanitizedValues);
        }

        @Override
        public double[] values() {
            return Arrays.copyOf(values, values.length);
        }

        public double[] values(int expectedLength) {
            double[] result = new double[expectedLength];
            double[] source = values();
            System.arraycopy(source, 0, result, 0, Math.min(source.length, expectedLength));
            return result;
        }
    }
}
