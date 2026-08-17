package com.seniors.justlevelingfork.common.feat.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import com.seniors.justlevelingfork.common.feat.FeatEffectDefinition;

public final class AttributeModifierFeatEffect
        implements FeatEffect {

    public static final AttributeModifierFeatEffect INSTANCE =
            new AttributeModifierFeatEffect();

    private AttributeModifierFeatEffect() {
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

        List<ParsedModifier> modifiers =
                parseAll(
                        effect);

        if (modifiers.isEmpty()) {
            return false;
        }

        for (ParsedModifier parsed
                : modifiers) {

            if (!BuiltInRegistries.ATTRIBUTE
                    .containsKey(
                            parsed.attributeId())) {

                return false;
            }

            Attribute attribute =
                    BuiltInRegistries.ATTRIBUTE.get(
                            parsed.attributeId());

            if (attribute == null
                    || player.getAttribute(attribute) == null) {

                return false;
            }
        }

        return true;
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
         * FeatAttributeModifierService derives these
         * modifiers from stored feat ranks.
         */
    }

    /**
     * Parses every attribute modifier defined by a feat.
     *
     * Preferred format:
     *
     * "modifiers": [
     *   { ... },
     *   { ... }
     * ]
     *
     * Legacy format is still supported:
     *
     * "attribute": "...",
     * "operation": "...",
     * "amount": 4
     */
    public static List<ParsedModifier> parseAll(
            FeatEffectDefinition effectDefinition) {

        if (effectDefinition == null) {
            return List.of();
        }

        JsonObject effect =
                effectDefinition.getData();

        if (effect == null) {
            return List.of();
        }

        /*
         * New multi-modifier format.
         */
        if (effect.has("modifiers")) {

            JsonElement modifiersElement =
                    effect.get("modifiers");

            if (modifiersElement == null
                    || !modifiersElement.isJsonArray()) {

                return List.of();
            }

            JsonArray array =
                    modifiersElement.getAsJsonArray();

            if (array.isEmpty()) {
                return List.of();
            }

            List<ParsedModifier> result =
                    new ArrayList<>();

            for (JsonElement element : array) {

                if (element == null
                        || !element.isJsonObject()) {

                    return List.of();
                }

                ParsedModifier parsed =
                        parseModifier(
                                element.getAsJsonObject());

                /*
                 * Be strict:
                 * one invalid modifier invalidates the whole effect.
                 */
                if (parsed == null) {
                    return List.of();
                }

                result.add(parsed);
            }

            return List.copyOf(result);
        }

        /*
         * Backward-compatible single-modifier format.
         */
        ParsedModifier legacy =
                parseModifier(effect);

        if (legacy == null) {
            return List.of();
        }

        return List.of(legacy);
    }

    private static ParsedModifier parseModifier(
            JsonObject json) {

        try {

            ResourceLocation attributeId =
                    ResourceLocation.tryParse(
                            GsonHelper.getAsString(
                                    json,
                                    "attribute"));

            if (attributeId == null) {
                return null;
            }

            double amount =
                    GsonHelper.getAsDouble(
                            json,
                            "amount");

            if (!Double.isFinite(amount)
                    || amount == 0.0D) {

                return null;
            }

            String operationName =
                    GsonHelper.getAsString(
                                    json,
                                    "operation",
                                    "addition")
                            .toLowerCase(Locale.ROOT);

            AttributeModifier.Operation operation =
                    parseOperation(
                            operationName);

            if (operation == null) {
                return null;
            }

            return new ParsedModifier(
                    attributeId,
                    amount,
                    operation);

        } catch (Exception ignored) {

            return null;
        }
    }

    private static AttributeModifier.Operation parseOperation(
            String value) {

        return switch (value) {

            case "addition",
                 "add" ->
                    AttributeModifier.Operation.ADDITION;

            case "multiply_base",
                 "multiply_base_value" ->
                    AttributeModifier.Operation.MULTIPLY_BASE;

            case "multiply_total",
                 "multiply_total_value" ->
                    AttributeModifier.Operation.MULTIPLY_TOTAL;

            default ->
                    null;
        };
    }

    public record ParsedModifier(
            ResourceLocation attributeId,
            double amount,
            AttributeModifier.Operation operation) {
    }
}