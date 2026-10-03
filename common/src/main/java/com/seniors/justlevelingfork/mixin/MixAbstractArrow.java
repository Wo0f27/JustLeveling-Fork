package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.event.ProjectileSkillEffects;
import com.seniors.justlevelingfork.common.ranged.RangedAttackContext;
import com.seniors.justlevelingfork.common.ranged.RangedShotCarrier;
import com.seniors.justlevelingfork.integration.crit.CritHitContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class MixAbstractArrow implements RangedShotCarrier {
    @Unique
    private RangedAttackContext.Shot justlevelingfork$rangedShot;

    @Unique
    private Entity justlevelingfork$rangedImpactTarget;

    @Unique
    private Vec3 justlevelingfork$rangedImpactPosition;

    @Unique
    private double justlevelingfork$previousBaseDamage;

    @Unique
    private boolean justlevelingfork$hasPreviousBaseDamage;

    @Override
    public void justlevelingfork$setRangedShot(RangedAttackContext.Shot shot) {
        this.justlevelingfork$rangedShot = shot;
    }

    @Override
    public RangedAttackContext.Shot justlevelingfork$getRangedShot() {
        return this.justlevelingfork$rangedShot;
    }

    @Override
    public void justlevelingfork$setRangedImpact(Entity target, Vec3 position) {
        this.justlevelingfork$rangedImpactTarget = target;
        this.justlevelingfork$rangedImpactPosition = position;
    }

    @Override
    public Entity justlevelingfork$getRangedImpactTarget() {
        return this.justlevelingfork$rangedImpactTarget;
    }

    @Override
    public Vec3 justlevelingfork$getRangedImpactPosition() {
        return this.justlevelingfork$rangedImpactPosition;
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void justlevelingfork$beforeArrowEntityHit(EntityHitResult hitResult, CallbackInfo callbackInfo) {
        this.justlevelingfork$setRangedImpact(hitResult.getEntity(), hitResult.getLocation());
        CritHitContext.push(hitResult.getEntity());
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        this.justlevelingfork$previousBaseDamage = arrow.getBaseDamage();
        this.justlevelingfork$hasPreviousBaseDamage = true;
        arrow.setBaseDamage(ProjectileSkillEffects.adjustedArrowBaseDamage(
                arrow, this.justlevelingfork$previousBaseDamage));
    }

    @Inject(method = "onHitEntity", at = @At("RETURN"))
    private void justlevelingfork$afterArrowEntityHit(EntityHitResult hitResult, CallbackInfo callbackInfo) {
        this.justlevelingfork$setRangedImpact(null, null);
        CritHitContext.pop();
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        ProjectileSkillEffects.afterArrowEntityHit(arrow, hitResult);
        if (this.justlevelingfork$hasPreviousBaseDamage) {
            arrow.setBaseDamage(this.justlevelingfork$previousBaseDamage);
            this.justlevelingfork$hasPreviousBaseDamage = false;
        }
    }
}
