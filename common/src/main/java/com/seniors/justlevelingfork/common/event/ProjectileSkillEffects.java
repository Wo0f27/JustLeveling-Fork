package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistryEffects;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;

public final class ProjectileSkillEffects {
    private ProjectileSkillEffects() {
    }

    public static double adjustedArrowBaseDamage(AbstractArrow arrow, double baseDamage) {
        if (!(arrow.getOwner() instanceof Player player)) {
            return baseDamage;
        }

        double adjustedDamage = baseDamage + player.getAttributeValue(RegistryAttributes.PROJECTILE_DAMAGE) / 5.0D;
        if (player instanceof ServerPlayer serverPlayer
                && player.isShiftKeyDown()
                && PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.STEALTH_MASTERY)) {
            adjustedDamage += baseDamage * (RegistrySkills.STEALTH_MASTERY.getValue()[2] - 1.0D);
        }
        return adjustedDamage;
    }

    public static void afterArrowEntityHit(AbstractArrow arrow, EntityHitResult hitResult) {
        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayer serverPlayer) || hitResult.getEntity() == null) {
            return;
        }

        new RegistryEffects.AddEffect(
                        serverPlayer,
                        PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.QUICK_REPOSITION),
                        MobEffects.MOVEMENT_SPEED)
                .add(
                        (int) (10.0D + 20.0D * RegistrySkills.QUICK_REPOSITION.getValue()[1]),
                        (int) (RegistrySkills.QUICK_REPOSITION.getValue()[0] - 1.0D));
    }
}
