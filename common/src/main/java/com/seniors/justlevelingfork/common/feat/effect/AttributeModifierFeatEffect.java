package com.seniors.justlevelingfork.common.feat.effect;

import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

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
            String choice) {

        if (player == null
                || progress == null
                || feat == null) {

            return false;
        }

        ParsedModifier parsed =
                parse(feat);

        if (parsed == null) {
            return false;
        }

        if (!BuiltInRegistries.ATTRIBUTE.containsKey(
                parsed.attributeId())) {

            return false;
        }

        Attribute attribute =
                BuiltInRegistries.ATTRIBUTE.get(
                        parsed.attributeId());

        /*
         * The attribute may exist globally but not actually
         * be present on Player.
         */
        return attribute != null
                && player.getAttribute(attribute) != null;
    }

    @Override
    public void apply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            String choice) {

        /*
         * Intentionally empty.
         *
         * Attribute modifiers are derived from the player's
         * stored feat ranks by FeatAttributeModifierService.
         *
         * FeatProgressionService increments the rank inside
         * PlayerProgressService.update(...), and the refresh
         * happens afterward.
         *
         * This also lets /reload alter existing modifiers.
         */
    }

    public static ParsedModifier parse(
            FeatDefinition feat) {

        if (feat == null) {
            return null;
        }

        try {

            JsonObject effect =
                    feat.getEffectData();

            ResourceLocation attributeId =
                    ResourceLocation.tryParse(
                            GsonHelper.getAsString(
                                    effect,
                                    "attribute"));

            if (attributeId == null) {
                return null;
            }

            double amount =
                    GsonHelper.getAsDouble(
                            effect,
                            "amount");

            if (!Double.isFinite(amount)
                    || amount == 0.0D) {

                return null;
            }

            String operationName =
                    GsonHelper.getAsString(
                                    effect,
                                    "operation",
                                    "addition")
                            .toLowerCase(Locale.ROOT);

            AttributeModifier.Operation operation =
                    parseOperation(operationName);

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