package com.seniors.justlevelingfork.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.seniors.justlevelingfork.common.ranged.RangedAttackContextService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.world.item.BowItem.class)
public abstract class MixBowItemRangedShot {
    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private void justlevelingfork$captureBowShot(
            AbstractArrow arrow, Entity shooter, float xRot, float yRot, float yRotOffset,
            float velocity, float inaccuracy, Operation<Void> original,
            ItemStack weapon, net.minecraft.world.level.Level level, LivingEntity user, int remainingUseTicks) {
        original.call(arrow, shooter, xRot, yRot, yRotOffset, velocity, inaccuracy);
        if (user instanceof ServerPlayer player) {
            RangedAttackContextService.capture(arrow, player, weapon);
        }
    }
}
