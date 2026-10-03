package com.seniors.justlevelingfork.common.feat;

/** Additive per-shot crit bonuses; never persisted on Apothic attributes. */
public final class SharpshooterCritBands {
    public record Bonus(double chance, float damage) {
    }

    private SharpshooterCritBands() {
    }

    public static Bonus forDistance(double distance) {
        if (distance >= 32.0D) {
            return new Bonus(0.15D, 0.25F);
        }
        if (distance >= 16.0D) {
            return new Bonus(0.10D, 0.15F);
        }
        return new Bonus(0.05D, 0.0F);
    }
}
