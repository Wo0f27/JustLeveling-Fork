package com.seniors.justlevelingfork.common.ranged;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Transient provenance attached to a projectile, not to the shooter's current equipment. */
public interface RangedShotCarrier {
    void justlevelingfork$setRangedShot(RangedAttackContext.Shot shot);

    RangedAttackContext.Shot justlevelingfork$getRangedShot();

    void justlevelingfork$setRangedImpact(Entity target, Vec3 position);

    Entity justlevelingfork$getRangedImpactTarget();

    Vec3 justlevelingfork$getRangedImpactPosition();
}
