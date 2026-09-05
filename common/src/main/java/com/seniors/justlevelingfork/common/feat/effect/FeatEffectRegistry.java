package com.seniors.justlevelingfork.common.feat.effect;

import com.seniors.justlevelingfork.Constants;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class FeatEffectRegistry {

    private static final Map<ResourceLocation, FeatEffect> EFFECTS =
            new LinkedHashMap<>();

    public static final ResourceLocation ATTRIBUTE_MODIFIER =
            id("attribute_modifier");

    public static final ResourceLocation ABILITY_SCORE_IMPROVEMENT =
            id("ability_score_improvement");

    public static final ResourceLocation GRANT_ARMOR_PROFICIENCY =
            id("grant_armor_proficiency");

    static {
        register(
                ABILITY_SCORE_IMPROVEMENT,
                AbilityScoreImprovementFeatEffect.INSTANCE);

        register(
                ATTRIBUTE_MODIFIER,
                AttributeModifierFeatEffect.INSTANCE);

        register(
                GRANT_ARMOR_PROFICIENCY,
                GrantArmorProficiencyFeatEffect.INSTANCE);
    }

    private FeatEffectRegistry() {
    }

    public static boolean register(
            ResourceLocation id,
            FeatEffect effect) {

        if (id == null || effect == null) {
            return false;
        }

        if (EFFECTS.containsKey(id)) {
            Constants.LOG.warn(
                    "Feat effect type {} is already registered.",
                    id);

            return false;
        }

        EFFECTS.put(id, effect);

        Constants.LOG.info(
                "Registered feat effect type {}.",
                id);

        return true;
    }

    public static FeatEffect get(ResourceLocation id) {
        return id == null
                ? null
                : EFFECTS.get(id);
    }

    public static boolean contains(ResourceLocation id) {
        return id != null
                && EFFECTS.containsKey(id);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(
                Constants.MOD_ID,
                path);
    }
}