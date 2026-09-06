package com.seniors.justlevelingfork.common.feat.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.common.feat.FeatChoiceCodec;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.feat.FeatEffectDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public final class GrantWeaponProficiencyFeatEffect
        implements FeatEffect {

    public static final GrantWeaponProficiencyFeatEffect INSTANCE =
            new GrantWeaponProficiencyFeatEffect();

    private static final int MAX_CHOICES = 64;

    private GrantWeaponProficiencyFeatEffect() {
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

        ParsedConfig config =
                parseConfig(effect);

        if (config == null) {
            return false;
        }

        if (config.choicesRequired() <= 0) {
            return !config.fixedProficiencies().isEmpty();
        }

        Set<ResourceLocation> selected =
                parseSelectedProficiencies(
                        config,
                        choice);

        return selected.size()
                == config.choicesRequired();
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
         * Weapon proficiency is derived dynamically from stored
         * feat ownership and the persisted feat choice by
         * FeatWeaponProficiencyService.
         */
    }

    public static ParsedConfig parseConfig(
            FeatEffectDefinition effectDefinition) {

        if (effectDefinition == null) {
            return null;
        }

        JsonObject data =
                effectDefinition.getData();

        if (data == null) {
            return null;
        }

        Set<ResourceLocation> fixed =
                parseResourceArray(
                        data,
                        "proficiencies");

        if (data.has("proficiencies")
                && fixed == null) {

            return null;
        }

        Set<ResourceLocation> allowed =
                parseResourceArray(
                        data,
                        "allowed_proficiencies");

        if (data.has("allowed_proficiencies")
                && allowed == null) {

            return null;
        }

        int choicesRequired;

        try {

            choicesRequired =
                    GsonHelper.getAsInt(
                            data,
                            "choices_required",
                            0);

        } catch (RuntimeException exception) {

            return null;
        }

        if (choicesRequired < 0
                || choicesRequired > MAX_CHOICES) {

            return null;
        }

        String choiceKey;

        try {

            choiceKey =
                    GsonHelper.getAsString(
                                    data,
                                    "choice_key",
                                    "weapons")
                            .trim();

        } catch (RuntimeException exception) {

            return null;
        }

        if (choiceKey.isBlank()) {
            return null;
        }

        Set<ResourceLocation> safeFixed =
                fixed == null
                        ? Set.of()
                        : fixed;

        Set<ResourceLocation> safeAllowed =
                allowed == null
                        ? Set.of()
                        : allowed;

        if (safeFixed.isEmpty()
                && choicesRequired <= 0) {

            return null;
        }

        if (choicesRequired > 0
                && safeAllowed.size()
                < choicesRequired) {

            return null;
        }

        return new ParsedConfig(
                safeFixed,
                safeAllowed,
                choicesRequired,
                choiceKey);
    }

    public static Set<ResourceLocation> parseSelectedProficiencies(
            ParsedConfig config,
            String choice) {

        if (config == null
                || config.choicesRequired() <= 0) {

            return Set.of();
        }

        String weaponChoice =
                FeatChoiceCodec.extract(
                        choice,
                        config.choiceKey());

        if (weaponChoice.isBlank()) {
            return Set.of();
        }

        String[] entries =
                weaponChoice.split(",");

        LinkedHashSet<ResourceLocation> result =
                new LinkedHashSet<>();

        for (String entry : entries) {

            if (entry == null
                    || entry.isBlank()) {

                return Set.of();
            }

            ResourceLocation proficiencyId =
                    ResourceLocation.tryParse(
                            entry.trim());

            if (proficiencyId == null
                    || !config.allowedProficiencies()
                    .contains(proficiencyId)) {

                return Set.of();
            }

            if (!result.add(proficiencyId)) {

                /*
                 * Duplicate choices do not count as separate
                 * weapon proficiencies.
                 */
                return Set.of();
            }
        }

        if (result.size()
                != config.choicesRequired()) {

            return Set.of();
        }

        return Set.copyOf(result);
    }

    private static Set<ResourceLocation> parseResourceArray(
            JsonObject data,
            String key) {

        if (!data.has(key)) {
            return null;
        }

        JsonElement element =
                data.get(key);

        if (element == null
                || !element.isJsonArray()) {

            return null;
        }

        JsonArray array =
                element.getAsJsonArray();

        if (array.isEmpty()) {
            return Set.of();
        }

        LinkedHashSet<ResourceLocation> result =
                new LinkedHashSet<>();

        for (JsonElement value : array) {

            if (value == null
                    || !value.isJsonPrimitive()) {

                return null;
            }

            ResourceLocation id =
                    ResourceLocation.tryParse(
                            value.getAsString().trim());

            if (id == null) {
                return null;
            }

            result.add(id);
        }

        return Set.copyOf(result);
    }

    public record ParsedConfig(
            Set<ResourceLocation> fixedProficiencies,
            Set<ResourceLocation> allowedProficiencies,
            int choicesRequired,
            String choiceKey) {
    }
}
