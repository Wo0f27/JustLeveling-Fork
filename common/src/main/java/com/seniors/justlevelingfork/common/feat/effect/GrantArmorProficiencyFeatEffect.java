package com.seniors.justlevelingfork.common.feat.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.feat.FeatEffectDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;

public final class GrantArmorProficiencyFeatEffect
        implements FeatEffect {

    public static final GrantArmorProficiencyFeatEffect INSTANCE =
            new GrantArmorProficiencyFeatEffect();

    private GrantArmorProficiencyFeatEffect() {
    }

    @Override
    public boolean canApply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {

        if (player == null
                || progress == null
                || feat == null
                || effect == null) {

            return false;
        }

        return !parseCategories(effect)
                .isEmpty();
    }

    @Override
    public void apply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {

        /*
         * Intentionally empty.
         *
         * Armor proficiency is derived dynamically from
         * stored feat ownership by
         * FeatArmorProficiencyService.
         *
         * This avoids persisting duplicate proficiency state.
         */
    }

    public static Set<ArmorCategory> parseCategories(
            FeatEffectDefinition effectDefinition) {

        if (effectDefinition == null) {
            return Set.of();
        }

        JsonObject effect =
                effectDefinition.getData();

        if (!effect.has("categories")
                || !effect.get("categories")
                .isJsonArray()) {

            return Set.of();
        }

        JsonArray array =
                effect.getAsJsonArray(
                        "categories");

        if (array.isEmpty()) {
            return Set.of();
        }

        EnumSet<ArmorCategory> result =
                EnumSet.noneOf(
                        ArmorCategory.class);

        for (JsonElement element : array) {

            /*
             * Be strict:
             * one malformed category invalidates
             * the entire effect.
             */
            if (element == null
                    || !element.isJsonPrimitive()) {

                return Set.of();
            }

            String categoryName =
                    element.getAsString()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT);

            if (categoryName.isBlank()) {
                return Set.of();
            }

            ArmorCategory category;

            try {

                category =
                        ArmorCategory.valueOf(
                                categoryName);

            } catch (IllegalArgumentException exception) {

                return Set.of();
            }

            result.add(category);
        }

        if (result.isEmpty()) {
            return Set.of();
        }

        return Set.copyOf(result);
    }
}