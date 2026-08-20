package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.effect.AttributeModifierFeatEffect;
import com.seniors.justlevelingfork.common.feat.effect.FeatEffectRegistry;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class FeatAttributeModifierService {

    /*
     * player UUID
     *   -> modifier UUID
     *      -> attribute ID
     */
    private static final Map<
            UUID,
            Map<UUID, ResourceLocation>>
            APPLIED =
            new HashMap<>();

    private FeatAttributeModifierService() {
    }

    public static void refresh(
            ServerPlayer player,
            PlayerProgress progress) {

        if (player == null
                || progress == null) {

            return;
        }

        /*
         * Rebuild all derived feat modifiers from scratch.
         */
        clear(player);

        Map<UUID, ResourceLocation> applied =
                new LinkedHashMap<>();

        for (FeatDefinition feat
                : FeatManager.INSTANCE.values()) {

            if (feat == null
                    || feat.getId() == null) {

                continue;
            }

            int rank =
                    progress.getFeatRank(
                            feat.getId().toString());

            if (rank <= 0) {
                continue;
            }

            List<FeatEffectDefinition> effects =
                    feat.getEffects();

            for (int effectIndex = 0;
                 effectIndex < effects.size();
                 effectIndex++) {

                FeatEffectDefinition effectDefinition =
                        effects.get(
                                effectIndex);

                if (!FeatEffectRegistry.ATTRIBUTE_MODIFIER
                        .equals(
                                effectDefinition.getType())) {

                    continue;
                }

                List<AttributeModifierFeatEffect.ParsedModifier>
                        modifiers =
                        AttributeModifierFeatEffect.parseAll(
                                effectDefinition);

                if (modifiers.isEmpty()) {

                    Constants.LOG.warn(
                            "Could not apply attribute modifier effect {} in feat {} because its data is invalid.",
                            effectIndex,
                            feat.getId());

                    continue;
                }

                for (int modifierIndex = 0;
                     modifierIndex < modifiers.size();
                     modifierIndex++) {

                    applyModifier(
                            player,
                            feat,
                            modifiers.get(modifierIndex),
                            rank,
                            progress.getCharacterLevel(),
                            effectIndex,
                            modifierIndex,
                            applied);
                }
            }
        }
    }

    private static void applyModifier(
            ServerPlayer player,
            FeatDefinition feat,
            AttributeModifierFeatEffect.ParsedModifier parsed,
            int rank,
            int characterLevel,
            int effectIndex,
            int modifierIndex,
            Map<UUID, ResourceLocation> applied) {

        if (!BuiltInRegistries.ATTRIBUTE.containsKey(
                parsed.attributeId())) {

            Constants.LOG.warn(
                    "Feat {} references unknown attribute {}.",
                    feat.getId(),
                    parsed.attributeId());

            return;
        }

        Attribute attribute =
                BuiltInRegistries.ATTRIBUTE.get(
                        parsed.attributeId());

        if (attribute == null) {
            return;
        }

        AttributeInstance instance =
                player.getAttribute(
                        attribute);

        if (instance == null) {

            Constants.LOG.warn(
                    "Feat {} references attribute {} which is not present on this player.",
                    feat.getId(),
                    parsed.attributeId());

            return;
        }

        /*
         * Repeatable feats scale linearly with rank.
         */
        double amountForCurrentLevel =
                parsed.amount()
                        + parsed.amountPerCharacterLevel()
                        * Math.max(
                        0,
                        characterLevel);

        double totalAmount =
                amountForCurrentLevel
                        * rank;

        /*
         * Index is included because one feat may now contain
         * more than one modifier, including multiple modifiers
         * targeting the same attribute.
         */
        UUID modifierId =
                modifierUuid(
                        feat.getId(),
                        parsed.attributeId(),
                        effectIndex,
                        modifierIndex);

        AttributeModifier modifier =
                new AttributeModifier(
                        modifierId,
                        "JLF feat: "
                                + feat.getId()
                                + " #"
                                + effectIndex
                                + ":"
                                + modifierIndex,
                        totalAmount,
                        parsed.operation());

        /*
         * Defensive cleanup.
         */
        instance.removeModifier(
                modifierId);

        instance.addTransientModifier(
                modifier);

        applied.put(
                modifierId,
                parsed.attributeId());
    }

    public static void clear(
            ServerPlayer player) {

        if (player == null) {
            return;
        }

        Map<UUID, ResourceLocation> applied =
                APPLIED.remove(
                        player.getUUID());

        if (applied == null
                || applied.isEmpty()) {

            return;
        }

        applied.forEach(
                (modifierId, attributeId) -> {

                    if (!BuiltInRegistries.ATTRIBUTE
                            .containsKey(attributeId)) {

                        return;
                    }

                    Attribute attribute =
                            BuiltInRegistries.ATTRIBUTE.get(
                                    attributeId);

                    if (attribute == null) {
                        return;
                    }

                    AttributeInstance instance =
                            player.getAttribute(
                                    attribute);

                    if (instance != null) {

                        instance.removeModifier(
                                modifierId);
                    }
                });
    }

    private static UUID modifierUuid(
            ResourceLocation featId,
            ResourceLocation attributeId,
            int effectIndex,
            int modifierIndex) {

        String key =
                "justlevelingfork:feat_attribute/"
                        + featId
                        + "/"
                        + attributeId
                        + "/"
                        + effectIndex
                        + "/"
                        + modifierIndex;

        return UUID.nameUUIDFromBytes(
                key.getBytes(
                        StandardCharsets.UTF_8));
    }
}