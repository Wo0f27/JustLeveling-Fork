package com.seniors.justlevelingfork.common.feat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Regression coverage for the live Sharpshooter distance bands. */
class SharpshooterDamageBandsTest {
    @Test
    void exactDistanceBoundaries() {
        assertBand(0.0D, 1.00F);
        assertBand(5.0D, 1.00F);
        assertBand(7.999D, 1.00F);
        assertBand(8.0D, 1.05F);
        assertBand(12.0D, 1.05F);
        assertBand(15.999D, 1.05F);
        assertBand(16.0D, 1.10F);
        assertBand(24.0D, 1.10F);
        assertBand(31.999D, 1.10F);
        assertBand(32.0D, 1.15F);
        assertBand(40.0D, 1.15F);
        assertBand(47.999D, 1.15F);
        assertBand(48.0D, 1.20F);
        assertBand(52.0D, 1.20F);
        assertBand(1000.0D, 1.20F);
    }

    private static void assertBand(double distance, float expected) {
        assertEquals(expected, SharpshooterDamageBands.damageMultiplier(distance),
                () -> "distance=" + distance);
    }
}
