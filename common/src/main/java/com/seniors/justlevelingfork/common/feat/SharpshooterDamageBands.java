package com.seniors.justlevelingfork.common.feat;

/** Pure SS1 launch-to-impact damage tiers. */
public final class SharpshooterDamageBands {
    private SharpshooterDamageBands() {
    }

    public static float damageMultiplier(double distance) {
        if (distance >= 48.0D) {
            return 1.20F;
        }
        if (distance >= 32.0D) {
            return 1.15F;
        }
        if (distance >= 16.0D) {
            return 1.10F;
        }
        if (distance >= 8.0D) {
            return 1.05F;
        }
        return 1.0F;
    }
}
