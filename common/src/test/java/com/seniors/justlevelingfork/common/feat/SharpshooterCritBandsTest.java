package com.seniors.justlevelingfork.common.feat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SharpshooterCritBandsTest {
    @Test
    void contextualCritBoundaries() {
        assertBand(0.0D, 0.05D, 0.00F);
        assertBand(5.0D, 0.05D, 0.00F);
        assertBand(12.0D, 0.05D, 0.00F);
        assertBand(15.999D, 0.05D, 0.00F);
        assertBand(16.0D, 0.10D, 0.15F);
        assertBand(24.0D, 0.10D, 0.15F);
        assertBand(31.999D, 0.10D, 0.15F);
        assertBand(32.0D, 0.15D, 0.25F);
        assertBand(40.0D, 0.15D, 0.25F);
        assertBand(48.0D, 0.15D, 0.25F);
        assertBand(52.0D, 0.15D, 0.25F);
        assertBand(1000.0D, 0.15D, 0.25F);
    }

    private static void assertBand(double distance, double chance, float damage) {
        SharpshooterCritBands.Bonus bonus = SharpshooterCritBands.forDistance(distance);
        assertEquals(chance, bonus.chance(), () -> "chance at distance=" + distance);
        assertEquals(damage, bonus.damage(), () -> "damage at distance=" + distance);
    }
}
