package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.integration.crit.CritHandledDamageSource;
import com.seniors.justlevelingfork.integration.crit.CritIntegrationService;
import com.seniors.justlevelingfork.integration.crit.SharpshooterCritIntegration;
import net.critical_strike.api.CriticalDamageSource;
import net.critical_strike.internal.CritLogic;
import net.critical_strike.internal.CriticalStriker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional: Critical Strike's only physical crit routine (melee and persistent arrows). */
@Pseudo
@Mixin(targets = "net.critical_strike.internal.CritLogic", remap = false)
public abstract class MixCriticalStrikeCritLogic {
    @Inject(method = "modifyDamage", at = @At("HEAD"), cancellable = true, remap = false)
    private static void justlevelingfork$useApothicCrits(
            CriticalStriker striker, DamageSource source, float amount,
            CallbackInfoReturnable<CritLogic.Result> callback) {
        if (!(source.getEntity() instanceof Player attacker)) {
            return;
        }

        CritIntegrationService.Context baseContext = CritIntegrationService.context(attacker, source);
        if (!(baseContext.target() instanceof LivingEntity livingTarget)) {
            // Apothic's LivingHurtEvent does not process non-living targets.
            // Leave those hits to Critical Strike's original path.
            return;
        }

        // Mark before rolling: a failed Critical Strike roll must not become a second Apothic roll.
        ((CritHandledDamageSource) source).justlevelingfork$markCritHandled(livingTarget);
        CritIntegrationService.Context effectiveContext = SharpshooterCritIntegration.apply(baseContext);
        float multiplier = CritIntegrationService.rollMultiplier(effectiveContext, livingTarget);
        ((CriticalDamageSource) source).rng_setCriticalDamageMultiplier(multiplier > 1.0F ? multiplier : 0.0F);
        callback.setReturnValue(multiplier > 1.0F
                ? new CritLogic.Result(source, amount * multiplier)
                : null);
    }
}
