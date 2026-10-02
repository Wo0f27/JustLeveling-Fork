package com.seniors.justlevelingfork.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.seniors.justlevelingfork.integration.MobileFeatIntegration;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class MixMobileLivingEntity {

    @Shadow
    protected abstract float getWaterSlowDown();

    @ModifyExpressionValue(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDepthStrider(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int justlevelingfork$mobileDepthStriderFloor(int realLevel) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return isMobileInWater(entity) ? Math.max(realLevel, 2) : realLevel;
    }

    @ModifyArg(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V",
                    ordinal = 1),
            index = 0)
    private float justlevelingfork$mobileLavaAcceleration(float acceleration) {
        LivingEntity entity = (LivingEntity) (Object) this;
        float mobileAcceleration = acceleration;
        if (isMobileInLava(entity)) {
            float effectiveLevel = entity.onGround() ? 2.0F : 1.0F;
            // Match water's effective Depth Strider II acceleration formula.
            mobileAcceleration = acceleration
                    + (entity.getSpeed() - acceleration) * effectiveLevel / 3.0F;
        }
        return mobileAcceleration;
    }

    @ModifyExpressionValue(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 1))
    private Vec3 justlevelingfork$mobileLavaShallowDrag(Vec3 vanilla) {
        return justlevelingfork$withLavaHorizontalDrag(vanilla);
    }

    @ModifyExpressionValue(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 justlevelingfork$mobileLavaDeepDrag(Vec3 vanilla) {
        return justlevelingfork$withLavaHorizontalDrag(vanilla);
    }

    @Unique
    private Vec3 justlevelingfork$withLavaHorizontalDrag(Vec3 vanilla) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Vec3 after = vanilla;
        double horizontalDrag = 0.5D;
        if (isMobileInLava(entity)) {
            float startingDrag = entity.isSprinting() ? 0.9F : getWaterSlowDown();
            float effectiveLevel = entity.onGround() ? 2.0F : 1.0F;
            horizontalDrag = startingDrag
                    + (0.54600006F - startingDrag) * effectiveLevel / 3.0F;

            // Vanilla already applied 0.5 horizontal drag. Keep its Y component.
            double correction = horizontalDrag / 0.5D;
            after = new Vec3(vanilla.x * correction, vanilla.y, vanilla.z * correction);
        }
        return after;
    }

    @Unique
    private static boolean isMobileInWater(LivingEntity entity) {
        return entity instanceof Player player
                && player.isInWater()
                && MobileFeatIntegration.hasMobile(player);
    }

    @Unique
    private static boolean isMobileInLava(LivingEntity entity) {
        return entity instanceof Player player
                && player.isInLava()
                && MobileFeatIntegration.hasMobile(player);
    }
}
