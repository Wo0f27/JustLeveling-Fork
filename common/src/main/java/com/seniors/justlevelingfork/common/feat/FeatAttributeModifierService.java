package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.effect.AttributeModifierFeatEffect;
import com.seniors.justlevelingfork.common.feat.effect.FeatEffectRegistry;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
     * Runtime record of transient modifiers that JLF applied.
     *
     * player UUID
     *   -> modifier UUID
     *      -> attribute ID
     *
     * Transient modifiers themselves are intentionally not
     * persisted. They are reconstructed from PlayerProgress
     * and the currently loaded feat datapacks.
     */
    private static final Map<
            UUID,
            Map<UUID, ResourceLocation>>
            APPLIED = new HashMap<>();

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
         * Always remove the previous derived state first.
         *
         * This allows:
         * - datapack amount changes
         * - operation changes
         * - attribute changes
         * - feat resets
         * - removed datapacks
         */
        clear(player);

        Map<UUID, ResourceLocation> applied =
                new LinkedHashMap<>();

        for (FeatDefinition feat
                : FeatManager.INSTANCE.values()) {

            if (feat == null
                    || feat.getId() == null
                    || !FeatEffectRegistry.ATTRIBUTE_MODIFIER
                    .equals(feat.getEffectType())) {

                continue;
            }

            int rank =
                    progress.getFeatRank(
                            feat.getId().toString());

            if (rank <= 0) {
                continue;
            }

            AttributeModifierFeatEffect.ParsedModifier parsed =
                    AttributeModifierFeatEffect.parse(
                            feat);

            if (parsed == null) {

                Constants.LOG.warn(
                        "Could not apply attribute modifier feat {} because its effect data is invalid.",
                        feat.getId());

                continue;
            }

            if (!BuiltInRegistries.ATTRIBUTE.containsKey(
                    parsed.attributeId())) {

                Constants.LOG.warn(
                        "Feat {} references unknown attribute {}.",
                        feat.getId(),
                        parsed.attributeId());

                continue;
            }

            Attribute attribute =
                    BuiltInRegistries.ATTRIBUTE.get(
                            parsed.attributeId());

            if (attribute == null) {
                continue;
            }

            AttributeInstance instance =
                    player.getAttribute(
                            attribute);

            if (instance == null) {

                Constants.LOG.warn(
                        "Feat {} references attribute {} which is not present on this player.",
                        feat.getId(),
                        parsed.attributeId());

                continue;
            }

            /*
             * Repeatable feats scale linearly by rank.
             *
             * Example:
             *
             * amount = 2
             * rank   = 3
             *
             * total modifier = 6
             */
            double totalAmount =
                    parsed.amount()
                            * rank;

            UUID modifierId =
                    modifierUuid(
                            feat.getId(),
                            parsed.attributeId());

            AttributeModifier modifier =
                    new AttributeModifier(
                            modifierId,
                            "JLF feat: "
                                    + feat.getId(),
                            totalAmount,
                            parsed.operation());

            /*
             * Defensive removal in case something triggered
             * an unusual refresh path.
             */
            instance.removeModifier(
                    modifierId);

            instance.addTransientModifier(
                    modifier);

            applied.put(
                    modifierId,
                    parsed.attributeId());
        }

        if (!applied.isEmpty()) {

            APPLIED.put(
                    player.getUUID(),
                    applied);
        }
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

        applied.forEach((modifierId, attributeId) -> {

            if (!BuiltInRegistries.ATTRIBUTE.containsKey(
                    attributeId)) {

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
            ResourceLocation attributeId) {

        String key =
                "justlevelingfork:feat_attribute/"
                        + featId
                        + "/"
                        + attributeId;

        return UUID.nameUUIDFromBytes(
                key.getBytes(
                        StandardCharsets.UTF_8));
    }
}