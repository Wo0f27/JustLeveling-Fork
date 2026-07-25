package com.seniors.justlevelingfork.common.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AptitudeExperienceTest {
    @Test
    void experienceCurveMatchesVanillaLevelThresholds() {
        assertEquals(0, AptitudeExperience.getExperienceForLevel(0));
        assertEquals(7, AptitudeExperience.getExperienceForLevel(1));
        assertEquals(315, AptitudeExperience.getExperienceForLevel(15));
        assertEquals(352, AptitudeExperience.getExperienceForLevel(16));
        assertEquals(1395, AptitudeExperience.getExperienceForLevel(30));
        assertEquals(1507, AptitudeExperience.getExperienceForLevel(31));
    }

    @Test
    void inverseCurveHandlesExactAndPartialLevels() {
        assertEquals(0, AptitudeExperience.getLevelForExperience(6));
        assertEquals(1, AptitudeExperience.getLevelForExperience(7));
        assertEquals(15, AptitudeExperience.getLevelForExperience(315));
        assertEquals(30, AptitudeExperience.getLevelForExperience(1395));
        assertEquals(30, AptitudeExperience.getLevelForExperience(1506));
        assertEquals(31, AptitudeExperience.getLevelForExperience(1507));
    }

    @Test
    void aptitudeCostUsesConfiguredFirstLevelOffset() {
        assertEquals(5, AptitudeExperience.requiredExperienceLevels(1, 5));
        assertEquals(AptitudeExperience.getExperienceForLevel(5), AptitudeExperience.requiredPoints(1, 5));
    }

    @Test
    void extremeExperienceInputsSaturateInsteadOfOverflowing() {
        assertEquals(Integer.MAX_VALUE, AptitudeExperience.requiredExperienceLevels(
                Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, AptitudeExperience.getExperienceForLevel(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, AptitudeExperience.xpBarCap(Integer.MAX_VALUE));
    }
}
