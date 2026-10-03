package com.seniors.justlevelingfork.common.ranged;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Immutable launch data and the current hit's damage context. Other features may consume this. */
public record RangedAttackContext(
        ServerPlayer shooter,
        AbstractArrow projectile,
        ItemStack sourceWeapon,
        Vec3 launchPosition,
        Vec3 impactPosition,
        double distance,
        DamageSource damageSource,
        LivingEntity target,
        boolean physicalRangedWeaponAttack) {

    public record Shot(UUID shooterId, ItemStack sourceWeapon, Vec3 launchPosition,
                       boolean rangedWeaponAtLaunch) {
        public Shot {
            sourceWeapon = sourceWeapon.copy();
        }

        @Override
        public ItemStack sourceWeapon() {
            return sourceWeapon.copy();
        }
    }
}
