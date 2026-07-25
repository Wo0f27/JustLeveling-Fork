package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownEnderpearl.class)
public class MixThrownEnderpearl {
    @Redirect(
            method = "onHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean justlevelingfork$applySafePort(Entity entity, DamageSource source, float amount) {
        if (entity instanceof ServerPlayer serverPlayer
                && PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.SAFE_PORT)) {
            return entity.hurt(source, 0.0F);
        }

        return entity.hurt(source, amount);
    }
}
