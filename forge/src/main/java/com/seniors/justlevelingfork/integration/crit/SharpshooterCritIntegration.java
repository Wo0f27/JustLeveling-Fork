package com.seniors.justlevelingfork.integration.crit;

import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.common.feat.SharpshooterCritBands;
import com.seniors.justlevelingfork.common.ranged.RangedAttackContext;
import com.seniors.justlevelingfork.common.ranged.RangedAttackContextService;
import com.seniors.justlevelingfork.integration.SharpshooterFeatIntegration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Applies only per-shot stat transforms to CI1's existing physical crit context. */
public final class SharpshooterCritIntegration {
    private SharpshooterCritIntegration() {
    }

    public static CritIntegrationService.Context apply(CritIntegrationService.Context base) {
        if (!(base.attacker() instanceof ServerPlayer shooter)) {
            return base;
        }
        boolean hasFeat = FeatProgressionService.hasFeat(shooter, SharpshooterFeatIntegration.SHARPSHOOTER);
        if (base.category() != CritIntegrationService.AttackCategory.PHYSICAL_PROJECTILE
                || !(base.target() instanceof LivingEntity livingTarget)) {
            return base;
        }
        RangedAttackContext shot = RangedAttackContextService.currentHit(base.source(), livingTarget);
        if (shot == null || shot.shooter() != shooter) {
            return base;
        }
        if (!shot.physicalRangedWeaponAttack()) {
            return base;
        }
        if (!hasFeat) {
            return base;
        }

        SharpshooterCritBands.Bonus bonus = SharpshooterCritBands.forDistance(shot.distance());
        return new CritIntegrationService.Context(
                base.attacker(), base.target(), base.source(), base.directEntity(),
                base.projectile(), base.category(),
                base.chance() + bonus.chance(), base.damage() + bonus.damage());
    }
}
