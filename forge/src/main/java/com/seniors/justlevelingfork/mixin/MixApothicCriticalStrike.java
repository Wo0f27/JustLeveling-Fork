package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.integration.crit.CritHandledDamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Suppress Apothic's roll only for this exact target's Critical Strike-handled damage. */
@Mixin(targets = "dev.shadowsoffire.attributeslib.impl.AttributeEvents", remap = false)
public abstract class MixApothicCriticalStrike {
    @Inject(method = "apothCriticalStrike", at = @At("HEAD"), cancellable = true, remap = false)
    private void justlevelingfork$avoidDoubleCrit(LivingHurtEvent event, CallbackInfo callback) {
        if (((CritHandledDamageSource) event.getSource())
                .justlevelingfork$isCritHandledFor(event.getEntity())) {
            callback.cancel();
        }
    }
}
