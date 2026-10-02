package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatCooldownService;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.common.proficiency.WeaponClassificationService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Server-authoritative melee damage reduction for Defensive Duelist. */
public final class DefensiveDuelistFeatIntegration {

    public static final ResourceLocation DEFENSIVE_DUELIST =
            new ResourceLocation(Constants.MOD_ID, "defensive_duelist");

    private static final ResourceLocation FINESSE =
            new ResourceLocation(Constants.MOD_ID, "weapons/properties/finesse");

    private static final float DAMAGE_MULTIPLIER = 0.70F;
    private static final int COOLDOWN_TICKS = 200;

    private DefensiveDuelistFeatIntegration() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(DefensiveDuelistFeatIntegration.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getAmount() <= 0.0F
                || !isMeleeHit(event.getSource())
                || !FeatProgressionService.hasFeat(player, DEFENSIVE_DUELIST)
                || !isWieldingFinesseWeapon(player)
                || !FeatCooldownService.isReady(player, DEFENSIVE_DUELIST)) {
            return;
        }

        float reducedDamage = event.getAmount() * DAMAGE_MULTIPLIER;
        if (reducedDamage >= event.getAmount()) {
            return;
        }

        if (FeatCooldownService.tryStart(player, DEFENSIVE_DUELIST, COOLDOWN_TICKS)) {
            event.setAmount(reducedDamage);
        }
    }

    private static boolean isWieldingFinesseWeapon(ServerPlayer player) {
        return WeaponClassificationService.matches(player.getMainHandItem(), FINESSE)
                || WeaponClassificationService.matches(player.getOffhandItem(), FINESSE);
    }

    private static boolean isMeleeHit(DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker) {
            return false;
        }

        return source.is(DamageTypes.PLAYER_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                || source.is(DamageTypes.STING);
    }
}
