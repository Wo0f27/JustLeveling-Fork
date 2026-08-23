package com.seniors.justlevelingfork.common.proficiency;

import com.seniors.justlevelingfork.Constants;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.seniors.justlevelingfork.common.player.ExternalAttributeService;

public final class EquipmentPenaltyAttributeService {

    /*
     * Fixed UUIDs ensure that each equipment penalty owns
     * exactly one modifier on each affected attribute.
     *
     * These modifiers are transient because they represent
     * currently equipped state, not persistent character data.
     */
    private static final UUID ATTACK_DAMAGE_ID =
            UUID.fromString(
                    "b3fba0d3-8192-4fb5-a90a-fb6389a6a101");

    private static final UUID ATTACK_SPEED_ID =
            UUID.fromString(
                    "b3fba0d3-8192-4fb5-a90a-fb6389a6a102");

    private static final UUID MOVEMENT_SPEED_ID =
            UUID.fromString(
                    "b3fba0d3-8192-4fb5-a90a-fb6389a6a103");

    private static final UUID PROJECTILE_DAMAGE_ID =
            UUID.fromString(
                    "b3fba0d3-8192-4fb5-a90a-fb6389a6a104");

    private static final UUID DRAW_SPEED_ID =
            UUID.fromString(
                    "b3fba0d3-8192-4fb5-a90a-fb6389a6a105");

    private EquipmentPenaltyAttributeService() {
    }

    public static void refresh(
            ServerPlayer player) {

        if (player == null) {
            return;
        }

        EquipmentPenaltyProfile profile =
                EquipmentPenaltyService
                        .resolve(player);

        applyReduction(
                player,
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_ID,
                "equipment_penalty_attack_damage",
                profile.attackDamageReduction());

        applyReduction(
                player,
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_ID,
                "equipment_penalty_attack_speed",
                profile.attackSpeedReduction());

        applyReduction(
                player,
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_ID,
                "equipment_penalty_movement_speed",
                profile.movementSpeedReduction());

        applyExternalReduction(
                player,
                "attributeslib",
                "arrow_damage",
                PROJECTILE_DAMAGE_ID,
                profile.projectileDamageReduction());

        applyExternalReduction(
                player,
                "attributeslib",
                "draw_speed",
                DRAW_SPEED_ID,
                profile.drawSpeedReduction());
    }

    public static void clear(
            ServerPlayer player) {

        if (player == null) {
            return;
        }

        removeModifier(
                player,
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_ID);

        removeModifier(
                player,
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_ID);

        removeModifier(
                player,
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_ID);

        ExternalAttributeService
                .applyTransientMultiplier(
                        player,
                        "attributeslib",
                        "arrow_damage",
                        0.0D,
                        PROJECTILE_DAMAGE_ID,
                        false);

        ExternalAttributeService
                .applyTransientMultiplier(
                        player,
                        "attributeslib",
                        "draw_speed",
                        0.0D,
                        DRAW_SPEED_ID,
                        false);
    }

    private static void applyReduction(
            ServerPlayer player,
            Attribute attribute,
            UUID uuid,
            String name,
            double reduction) {

        AttributeInstance instance =
                player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        /*
         * Resolver values use positive fractions:
         *
         * 0.15 = 15% reduction
         *
         * Attribute MULTIPLY_TOTAL needs:
         *
         * -0.15 = final value * 0.85
         */
        double safeReduction =
                Math.max(
                        0.0D,
                        Math.min(
                                reduction,
                                0.95D));

        double modifierAmount =
                -safeReduction;

        AttributeModifier existing =
                instance.getModifier(uuid);

        /*
         * No penalty should be active.
         */
        if (safeReduction <= 0.0D) {

            if (existing != null) {
                instance.removeModifier(
                        existing);
            }

            return;
        }

        /*
         * Avoid removing/re-adding the same modifier every
         * five ticks when equipment state has not changed.
         */
        if (existing != null
                && Double.compare(
                existing.getAmount(),
                modifierAmount) == 0
                && existing.getOperation()
                == AttributeModifier.Operation
                .MULTIPLY_TOTAL) {

            return;
        }

        if (existing != null) {

            instance.removeModifier(
                    existing);
        }

        instance.addTransientModifier(
                new AttributeModifier(
                        uuid,
                        Constants.MOD_ID
                                + ":"
                                + name,
                        modifierAmount,
                        AttributeModifier.Operation
                                .MULTIPLY_TOTAL));
    }

    private static void removeModifier(
            ServerPlayer player,
            Attribute attribute,
            UUID uuid) {

        AttributeInstance instance =
                player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        AttributeModifier modifier =
                instance.getModifier(uuid);

        if (modifier != null) {

            instance.removeModifier(
                    modifier);
        }
    }

    private static void applyExternalReduction(
            ServerPlayer player,
            String namespace,
            String path,
            UUID uuid,
            double reduction) {

        double safeReduction =
                Math.max(
                        0.0D,
                        Math.min(
                                reduction,
                                0.95D));

        ExternalAttributeService
                .applyTransientMultiplier(
                        player,
                        namespace,
                        path,
                        -safeReduction,
                        uuid,
                        safeReduction > 0.0D);
    }
}