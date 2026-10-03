package com.seniors.justlevelingfork.integration.crit;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Transient, per-DamageSource marker; never serialized or shared between hits. */
public interface CritHandledDamageSource {
    void justlevelingfork$markCritHandled(Entity target);

    boolean justlevelingfork$isCritHandledFor(LivingEntity target);

    void justlevelingfork$clearCritHandled();
}
