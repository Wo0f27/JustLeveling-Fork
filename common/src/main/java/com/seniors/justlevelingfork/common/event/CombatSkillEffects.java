package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.common.player.SkillMessageService;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class CombatSkillEffects {
    private CombatSkillEffects() {
    }

    public static float adjustPlayerAttackDamage(Player player, Entity target, float damage) {
        float adjustedDamage = damage;
        if (player instanceof ServerPlayer serverPlayer) {
            adjustedDamage += PlayerProgressService.consumeCounterAttackDamage(serverPlayer);
        }

        boolean vanillaCritical = isVanillaCritical(player, target);
        boolean berserkerCritical = isBerserkerCritical(player);
        boolean critical = vanillaCritical || berserkerCritical;
        if (!critical) {
            return adjustedDamage;
        }

        float criticalDamage = (float) player.getAttributeValue(RegistryAttributes.CRITICAL_DAMAGE);
        if (criticalDamage > 0.0F) {
            adjustedDamage *= vanillaCritical ? (1.5F + criticalDamage) / 1.5F : 1.0F + criticalDamage;
        }

        if (berserkerCritical && (player.onGround() || player.isInWater()) && !vanillaCritical) {
            adjustedDamage *= 1.5F;
        }

        if (player instanceof ServerPlayer serverPlayer
                && PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.CRITICAL_ROLL)) {
            adjustedDamage = applyCriticalRoll(serverPlayer, adjustedDamage);
        }

        return adjustedDamage;
    }

    public static void afterLivingHurt(LivingEntity target, DamageSource source, boolean hurt) {
        if (!hurt
                || !(target instanceof ServerPlayer player)
                || !(source.getEntity() instanceof LivingEntity attacker)
                || attacker.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
            return;
        }

        float sourceDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float counterDamage = (float) (sourceDamage * RegistrySkills.COUNTER_ATTACK.getValue()[1] / 100.0D);
        PlayerProgressService.armCounterAttack(player, counterDamage);
    }

    private static boolean isVanillaCritical(Player player, Entity target) {
        return player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.isPassenger()
                && !player.isSprinting()
                && target instanceof LivingEntity;
    }

    private static boolean isBerserkerCritical(Player player) {
        return player instanceof ServerPlayer serverPlayer
                && PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.BERSERKER)
                && player.getHealth() <= player.getMaxHealth() * (float) (RegistrySkills.BERSERKER.getValue()[0] / 100.0D);
    }

    private static float applyCriticalRoll(ServerPlayer player, float damage) {
        int dice = ThreadLocalRandom.current().nextInt(7);
        if (dice == 1) {
            SkillMessageService.send(player, "overlay.skill.justlevelingfork.critical_roll_1", 0);
            return damage / (1.0F + 1.0F / (float) RegistrySkills.CRITICAL_ROLL.getValue()[1]);
        }
        if (dice == 6) {
            SkillMessageService.send(player, "overlay.skill.justlevelingfork.critical_roll_6", 0);
            return damage * (float) RegistrySkills.CRITICAL_ROLL.getValue()[0];
        }
        return damage;
    }
}
