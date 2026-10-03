package com.seniors.justlevelingfork.integration.crit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.registries.ForgeRegistries;

/** Apothic-backed critical values for damage already owned by Critical Strike. */
public final class CritIntegrationService {
    private static final ResourceLocation CRIT_CHANCE = new ResourceLocation("attributeslib", "crit_chance");
    private static final ResourceLocation CRIT_DAMAGE = new ResourceLocation("attributeslib", "crit_damage");

    public enum AttackCategory {
        MELEE,
        PHYSICAL_PROJECTILE
    }

    /** Transient hit context; future feat modifiers can transform chance/damage here, not player attributes. */
    public record Context(Player attacker, Entity target, DamageSource source, Entity directEntity,
                          AbstractArrow projectile, AttackCategory category, double chance, float damage) {
    }

    private CritIntegrationService() {
    }

    public static Context context(Player attacker, DamageSource source) {
        Attribute chanceAttribute = ForgeRegistries.ATTRIBUTES.getValue(CRIT_CHANCE);
        Attribute damageAttribute = ForgeRegistries.ATTRIBUTES.getValue(CRIT_DAMAGE);
        if (chanceAttribute == null || damageAttribute == null) {
            throw new IllegalStateException("JLF crit bridge requires Apothic Attributes 1.3.7 attributes");
        }

        Entity direct = source.getDirectEntity();
        AbstractArrow arrow = direct instanceof AbstractArrow projectile ? projectile : null;
        AttackCategory category = arrow == null ? AttackCategory.MELEE : AttackCategory.PHYSICAL_PROJECTILE;
        return new Context(attacker, CritHitContext.target(), source, direct, arrow, category,
                attacker.getAttributeValue(chanceAttribute), (float) attacker.getAttributeValue(damageAttribute));
    }

    /** Exact Apothic Attributes 1.20.1-1.3.7 overcrit loop, including its target RNG and float arithmetic. */
    public static float rollMultiplier(Context context, LivingEntity livingTarget) {
        RandomSource random = livingTarget.getRandom();
        double chance = context.chance();
        float damage = context.damage();
        float multiplier = 1.0F;
        while (random.nextFloat() <= chance && damage > 1.0F) {
            chance--;
            multiplier *= damage;
            damage *= 0.85F;
        }
        return multiplier;
    }
}
