package com.seniors.justlevelingfork.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.seniors.justlevelingfork.common.ranged.RangedAttackContextService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.world.item.CrossbowItem.class)
public abstract class MixCrossbowItemRangedShot {
    @WrapOperation(method = "shootProjectile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/CrossbowItem;getArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"))
    private static AbstractArrow justlevelingfork$captureCrossbowShot(
            Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammunition,
            Operation<AbstractArrow> original,
            Level firingLevel, LivingEntity user, InteractionHand hand, ItemStack firingWeapon,
            ItemStack loadedProjectile, float soundPitch, boolean creative, float velocity,
            float inaccuracy, float angle) {
        AbstractArrow arrow = original.call(level, shooter, weapon, ammunition);
        if (user instanceof ServerPlayer player) {
            RangedAttackContextService.capture(arrow, player, firingWeapon);
        }
        return arrow;
    }
}
