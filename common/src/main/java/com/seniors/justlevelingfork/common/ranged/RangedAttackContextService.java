package com.seniors.justlevelingfork.common.ranged;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.proficiency.WeaponClassificationService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Captures bow/crossbow arrow launches and resolves only their active entity-hit damage. */
public final class RangedAttackContextService {
    public static final ResourceLocation RANGED_PROPERTY =
            new ResourceLocation(Constants.MOD_ID, "weapons/properties/ranged");

    private RangedAttackContextService() {
    }

    public static void capture(AbstractArrow arrow, ServerPlayer shooter, ItemStack weapon) {
        if (arrow.level().isClientSide || weapon.isEmpty()) {
            return;
        }
        ((RangedShotCarrier) arrow).justlevelingfork$setRangedShot(new RangedAttackContext.Shot(
                shooter.getUUID(), weapon, arrow.position(),
                WeaponClassificationService.matches(weapon, RANGED_PROPERTY)));
    }

    public static RangedAttackContext currentHit(DamageSource source, LivingEntity target) {
        if (!(source.getEntity() instanceof ServerPlayer shooter)
                || !(source.getDirectEntity() instanceof AbstractArrow arrow)
                || arrow.getOwner() != shooter) {
            return null;
        }
        RangedShotCarrier carrier = (RangedShotCarrier) arrow;
        RangedAttackContext.Shot shot = carrier.justlevelingfork$getRangedShot();
        Vec3 impact = carrier.justlevelingfork$getRangedImpactPosition();
        if (shot == null || impact == null || carrier.justlevelingfork$getRangedImpactTarget() != target
                || !shot.shooterId().equals(shooter.getUUID())) {
            return null;
        }
        return new RangedAttackContext(shooter, arrow, shot.sourceWeapon(), shot.launchPosition(),
                impact, shot.launchPosition().distanceTo(impact), source, target,
                shot.rangedWeaponAtLaunch());
    }
}
