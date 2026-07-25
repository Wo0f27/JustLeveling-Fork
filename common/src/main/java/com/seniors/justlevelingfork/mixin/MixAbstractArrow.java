package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.event.ProjectileSkillEffects;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class MixAbstractArrow {
    @Unique
    private double justlevelingfork$previousBaseDamage;

    @Unique
    private boolean justlevelingfork$hasPreviousBaseDamage;

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void justlevelingfork$beforeArrowEntityHit(EntityHitResult hitResult, CallbackInfo callbackInfo) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        this.justlevelingfork$previousBaseDamage = arrow.getBaseDamage();
        this.justlevelingfork$hasPreviousBaseDamage = true;
        arrow.setBaseDamage(ProjectileSkillEffects.adjustedArrowBaseDamage(
                arrow, this.justlevelingfork$previousBaseDamage));
    }

    @Inject(method = "onHitEntity", at = @At("RETURN"))
    private void justlevelingfork$afterArrowEntityHit(EntityHitResult hitResult, CallbackInfo callbackInfo) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        ProjectileSkillEffects.afterArrowEntityHit(arrow, hitResult);
        if (this.justlevelingfork$hasPreviousBaseDamage) {
            arrow.setBaseDamage(this.justlevelingfork$previousBaseDamage);
            this.justlevelingfork$hasPreviousBaseDamage = false;
        }
    }
}
