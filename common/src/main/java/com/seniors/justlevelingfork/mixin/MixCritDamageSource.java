package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.integration.crit.CritHandledDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public abstract class MixCritDamageSource implements CritHandledDamageSource {
    @Unique private boolean justlevelingfork$critHandled;
    @Unique private Entity justlevelingfork$critTarget;

    @Override
    public void justlevelingfork$markCritHandled(Entity target) {
        this.justlevelingfork$critHandled = true;
        this.justlevelingfork$critTarget = target;
    }

    @Override
    public boolean justlevelingfork$isCritHandledFor(LivingEntity target) {
        return this.justlevelingfork$critHandled && this.justlevelingfork$critTarget == target;
    }

    @Override
    public void justlevelingfork$clearCritHandled() {
        this.justlevelingfork$critHandled = false;
        this.justlevelingfork$critTarget = null;
    }
}
