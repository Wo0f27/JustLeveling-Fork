package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.common.feat.SharpshooterDamageBands;
import com.seniors.justlevelingfork.common.ranged.RangedAttackContext;
import com.seniors.justlevelingfork.common.ranged.RangedAttackContextService;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Server-only pre-armor damage adjustment for proven ranged-weapon arrow hits. */
public final class SharpshooterFeatIntegration {
    public static final ResourceLocation SHARPSHOOTER =
            new ResourceLocation(Constants.MOD_ID, "sharpshooter");

    private SharpshooterFeatIntegration() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(SharpshooterFeatIntegration.class);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide || event.getAmount() <= 0.0F) {
            return;
        }
        RangedAttackContext context =
                RangedAttackContextService.currentHit(event.getSource(), event.getEntity());
        if (context == null || !context.physicalRangedWeaponAttack()
                || !FeatProgressionService.hasFeat(context.shooter(), SHARPSHOOTER)) {
            return;
        }
        event.setAmount(event.getAmount() * SharpshooterDamageBands.damageMultiplier(context.distance()));
    }
}
