package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBullet.class)
public abstract class MixShulkerBullet {
    @Unique
    private final ShulkerBullet justlevelingfork$bullet = (ShulkerBullet) (Object) this;

    @Inject(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    protected void justlevelingfork$onHitEntity(EntityHitResult hitResult, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        Entity target = hitResult.getEntity();
        Entity owner = this.justlevelingfork$bullet.getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity livingEntity ? livingEntity : null;
        boolean hurt = target.hurt(
                this.justlevelingfork$bullet.damageSources().mobProjectile(this.justlevelingfork$bullet, livingOwner),
                4.0F);
        if (!hurt) {
            return;
        }

        if (livingOwner != null) {
            this.justlevelingfork$bullet.doEnchantDamageEffects(livingOwner, target);
        }

        if (target instanceof Player player && justlevelingfork$blocksLevitation(player)) {
            return;
        }

        if (target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200), owner != null ? owner : this.justlevelingfork$bullet);
        }
    }

    @Unique
    private static boolean justlevelingfork$blocksLevitation(Player player) {
        return player instanceof ServerPlayer serverPlayer
                && PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.TURTLE_SHIELD);
    }
}
