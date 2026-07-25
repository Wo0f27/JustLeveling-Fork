package com.seniors.justlevelingfork.common.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.seniors.justlevelingfork.common.config.PassiveConfigService.PassiveConfig;
import com.seniors.justlevelingfork.common.config.SkillConfigService.SkillConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ConfigSanitizationTest {
    @Test
    void fiveRankPassivesEndAtAptitudeLevel32() {
        int[] expectedLevels = {8, 14, 20, 26, 32};

        assertArrayEquals(expectedLevels, PassiveConfigService.levels5());
        assertArrayEquals(
                expectedLevels,
                PassiveConfigService.defaultConfigs().get("break_speed").levels());
    }

    @Test
    void legacyBreakSpeedTypoIsMigratedWithoutDiscardingItsConfiguredValue() {
        Map<String, PassiveConfig> input = new LinkedHashMap<>();
        input.put("break_speed", new PassiveConfig(0.75D, new int[] {8, 14, 20, 26, 3}));

        PassiveConfig migrated = PassiveConfigService.sanitize(input).get("break_speed");

        assertEquals(0.75D, migrated.value());
        assertArrayEquals(new int[] {8, 14, 20, 26, 32}, migrated.levels());
    }

    @Test
    void passiveConfigDropsUnknownKeysAndBoundsValues() {
        Map<String, PassiveConfig> input = new LinkedHashMap<>();
        input.put("unknown", new PassiveConfig(10.0D, new int[] {2}));
        input.put(
                "attack_damage",
                new PassiveConfig(
                        Double.NaN,
                        IntStream.range(0, 100).map(index -> index == 0 ? -5 : 5000).toArray()));

        Map<String, PassiveConfig> sanitized = PassiveConfigService.sanitize(input);
        PassiveConfig attackDamage = sanitized.get("attack_damage");

        assertFalse(sanitized.containsKey("unknown"));
        assertEquals(PassiveConfigService.defaultConfigs().get("attack_damage").value(), attackDamage.value());
        assertEquals(64, attackDamage.levels().length);
        assertEquals(1, attackDamage.levels()[0]);
        assertEquals(CommonConfigService.MAX_APTITUDE_LEVEL, attackDamage.levels()[1]);
    }

    @Test
    void skillConfigDropsUnknownKeysAndIgnoresExtraValues() {
        Map<String, SkillConfig> input = new LinkedHashMap<>();
        input.put("unknown", new SkillConfig(1, new double[] {1.0D}));
        input.put("berserker", new SkillConfig(5000, new double[] {Double.NaN, 3.0D}));

        Map<String, SkillConfig> sanitized = SkillConfigService.sanitize(input);
        SkillConfig berserker = sanitized.get("berserker");

        assertFalse(sanitized.containsKey("unknown"));
        assertEquals(CommonConfigService.MAX_APTITUDE_LEVEL, berserker.requiredLevel());
        assertEquals(1, berserker.values().length);
        assertEquals(0.0D, berserker.values()[0]);
    }
}
