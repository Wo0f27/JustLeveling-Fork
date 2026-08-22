package com.seniors.justlevelingfork.common.proficiency;

import com.seniors.justlevelingfork.registry.RegistryWeaponProficiencies;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class EquipmentPenaltyService {

    /*
     * Initial balance values.
     *
     * These are intentionally centralized so they can be
     * tuned later without changing the resolver architecture.
     */

    private static final double MELEE_ATTACK_DAMAGE = 0.15D;
    private static final double MELEE_ATTACK_SPEED = 0.10D;
    private static final double MELEE_CAST_TIME = 0.20D;
    private static final double MELEE_MANA_COST = 0.20D;

    private static final double RANGED_PROJECTILE_DAMAGE = 0.15D;
    private static final double RANGED_DRAW_SPEED = 0.10D;
    private static final double RANGED_CAST_TIME = 0.20D;
    private static final double RANGED_MANA_COST = 0.20D;

    private static final double CASTING_STAFF_ATTACK_DAMAGE = 0.10D;
    private static final double CASTING_STAFF_ATTACK_SPEED = 0.05D;
    private static final double CASTING_STAFF_CAST_TIME = 0.25D;
    private static final double CASTING_STAFF_MANA_COST = 0.25D;

    private static final double LIGHT_ATTACK_SPEED = 0.05D;
    private static final double LIGHT_MOVEMENT_SPEED = 0.05D;
    private static final double LIGHT_CAST_TIME = 0.10D;
    private static final double LIGHT_MANA_COST = 0.10D;

    private static final double MEDIUM_ATTACK_SPEED = 0.10D;
    private static final double MEDIUM_MOVEMENT_SPEED = 0.10D;
    private static final double MEDIUM_CAST_TIME = 0.20D;
    private static final double MEDIUM_MANA_COST = 0.20D;

    private static final double HEAVY_ATTACK_SPEED = 0.15D;
    private static final double HEAVY_MOVEMENT_SPEED = 0.15D;
    private static final double HEAVY_CAST_TIME = 0.30D;
    private static final double HEAVY_MANA_COST = 0.30D;

    private static final double SHIELD_ATTACK_SPEED = 0.05D;
    private static final double SHIELD_CAST_TIME = 0.10D;
    private static final double SHIELD_MANA_COST = 0.10D;

    /*
     * Pure ranged weapon families.
     *
     * Broad SIMPLE/MARTIAL tags are deliberately not used
     * because they do not tell us how the weapon is used.
     */
    private static final Set<ResourceLocation> RANGED_TYPES =
            Set.of(
                    RegistryWeaponProficiencies.LIGHT_CROSSBOW,
                    RegistryWeaponProficiencies.DART,
                    RegistryWeaponProficiencies.SHORTBOW,
                    RegistryWeaponProficiencies.SLING,
                    RegistryWeaponProficiencies.BLOWGUN,
                    RegistryWeaponProficiencies.HAND_CROSSBOW,
                    RegistryWeaponProficiencies.HEAVY_CROSSBOW,
                    RegistryWeaponProficiencies.LONGBOW,
                    RegistryWeaponProficiencies.NET
            );

    private EquipmentPenaltyService() {
    }

    public static EquipmentPenaltyProfile resolve(
            ServerPlayer player) {

        if (player == null) {

            return new EquipmentPenaltyProfile(
                    false,
                    WeaponPenaltyType.NONE,
                    false,
                    WeaponPenaltyType.NONE,
                    null,
                    false,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }

        ItemStack mainHand =
                WeaponUsageService
                        .getMainHandWeapon(player);

        ItemStack offHand =
                WeaponUsageService
                        .getOffhandWeapon(player);

        WeaponPenaltyType mainType =
                classifyWeapon(mainHand);

        WeaponPenaltyType offType =
                classifyWeapon(offHand);

        boolean mainNonProficient =
                !mainHand.isEmpty()
                        && !WeaponProficiencyService
                        .isProficient(
                                player,
                                mainHand);

        boolean offNonProficient =
                !offHand.isEmpty()
                        && !WeaponProficiencyService
                        .isProficient(
                                player,
                                offHand);

        ArmorCategory highestArmor =
                highestNonProficientArmor(
                        ArmorUsageService
                                .getNonProficientWornArmorCategories(
                                        player));

        boolean nonProficientShield =
                ArmorUsageService
                        .isUsingNonProficientShield(
                                player);

        MutablePenalties penalties =
                new MutablePenalties();

        if (mainNonProficient) {

            applyWeaponPenalty(
                    penalties,
                    mainType);
        }

        if (offNonProficient) {

            applyWeaponPenalty(
                    penalties,
                    offType);
        }

        applyArmorPenalty(
                penalties,
                highestArmor);

        if (nonProficientShield) {

            penalties.attackSpeedReduction =
                    strongest(
                            penalties.attackSpeedReduction,
                            SHIELD_ATTACK_SPEED);

            penalties.castTimeIncrease =
                    strongest(
                            penalties.castTimeIncrease,
                            SHIELD_CAST_TIME);

            penalties.manaCostIncrease =
                    strongest(
                            penalties.manaCostIncrease,
                            SHIELD_MANA_COST);
        }

        return new EquipmentPenaltyProfile(
                mainNonProficient,
                mainType,
                offNonProficient,
                offType,
                highestArmor,
                nonProficientShield,
                penalties.attackDamageReduction,
                penalties.attackSpeedReduction,
                penalties.movementSpeedReduction,
                penalties.projectileDamageReduction,
                penalties.drawSpeedReduction,
                penalties.castTimeIncrease,
                penalties.manaCostIncrease);
    }

    public static WeaponPenaltyType classifyWeapon(
            ItemStack stack) {

        if (stack == null
                || stack.isEmpty()
                || !WeaponClassificationService
                .isClassifiedWeapon(stack)) {

            return WeaponPenaltyType.NONE;
        }

        /*
         * Casting Staff wins over any overlapping broad
         * Simple/Martial classification.
         */
        if (WeaponClassificationService.matches(
                stack,
                RegistryWeaponProficiencies
                        .CASTING_STAFF)) {

            return WeaponPenaltyType
                    .CASTING_STAFF;
        }

        for (ResourceLocation rangedType
                : RANGED_TYPES) {

            if (WeaponClassificationService.matches(
                    stack,
                    rangedType)) {

                return WeaponPenaltyType.RANGED;
            }
        }

        /*
         * Every remaining classified weapon is treated
         * as melee for this first resolver.
         */
        return WeaponPenaltyType.MELEE;
    }

    private static void applyWeaponPenalty(
            MutablePenalties penalties,
            WeaponPenaltyType type) {

        if (penalties == null
                || type == null) {
            return;
        }

        switch (type) {

            case MELEE -> {

                penalties.attackDamageReduction =
                        strongest(
                                penalties.attackDamageReduction,
                                MELEE_ATTACK_DAMAGE);

                penalties.attackSpeedReduction =
                        strongest(
                                penalties.attackSpeedReduction,
                                MELEE_ATTACK_SPEED);

                penalties.castTimeIncrease =
                        strongest(
                                penalties.castTimeIncrease,
                                MELEE_CAST_TIME);

                penalties.manaCostIncrease =
                        strongest(
                                penalties.manaCostIncrease,
                                MELEE_MANA_COST);
            }

            case RANGED -> {

                penalties.projectileDamageReduction =
                        strongest(
                                penalties.projectileDamageReduction,
                                RANGED_PROJECTILE_DAMAGE);

                penalties.drawSpeedReduction =
                        strongest(
                                penalties.drawSpeedReduction,
                                RANGED_DRAW_SPEED);

                penalties.castTimeIncrease =
                        strongest(
                                penalties.castTimeIncrease,
                                RANGED_CAST_TIME);

                penalties.manaCostIncrease =
                        strongest(
                                penalties.manaCostIncrease,
                                RANGED_MANA_COST);
            }

            case CASTING_STAFF -> {

                penalties.attackDamageReduction =
                        strongest(
                                penalties.attackDamageReduction,
                                CASTING_STAFF_ATTACK_DAMAGE);

                penalties.attackSpeedReduction =
                        strongest(
                                penalties.attackSpeedReduction,
                                CASTING_STAFF_ATTACK_SPEED);

                penalties.castTimeIncrease =
                        strongest(
                                penalties.castTimeIncrease,
                                CASTING_STAFF_CAST_TIME);

                penalties.manaCostIncrease =
                        strongest(
                                penalties.manaCostIncrease,
                                CASTING_STAFF_MANA_COST);
            }

            case NONE -> {
                // No classified weapon penalty.
            }
        }
    }

    private static void applyArmorPenalty(
            MutablePenalties penalties,
            ArmorCategory category) {

        if (penalties == null
                || category == null) {
            return;
        }

        switch (category) {

            case LIGHT -> {

                penalties.attackSpeedReduction =
                        strongest(
                                penalties.attackSpeedReduction,
                                LIGHT_ATTACK_SPEED);

                penalties.movementSpeedReduction =
                        strongest(
                                penalties.movementSpeedReduction,
                                LIGHT_MOVEMENT_SPEED);

                penalties.castTimeIncrease =
                        strongest(
                                penalties.castTimeIncrease,
                                LIGHT_CAST_TIME);

                penalties.manaCostIncrease =
                        strongest(
                                penalties.manaCostIncrease,
                                LIGHT_MANA_COST);
            }

            case MEDIUM -> {

                penalties.attackSpeedReduction =
                        strongest(
                                penalties.attackSpeedReduction,
                                MEDIUM_ATTACK_SPEED);

                penalties.movementSpeedReduction =
                        strongest(
                                penalties.movementSpeedReduction,
                                MEDIUM_MOVEMENT_SPEED);

                penalties.castTimeIncrease =
                        strongest(
                                penalties.castTimeIncrease,
                                MEDIUM_CAST_TIME);

                penalties.manaCostIncrease =
                        strongest(
                                penalties.manaCostIncrease,
                                MEDIUM_MANA_COST);
            }

            case HEAVY -> {

                penalties.attackSpeedReduction =
                        strongest(
                                penalties.attackSpeedReduction,
                                HEAVY_ATTACK_SPEED);

                penalties.movementSpeedReduction =
                        strongest(
                                penalties.movementSpeedReduction,
                                HEAVY_MOVEMENT_SPEED);

                penalties.castTimeIncrease =
                        strongest(
                                penalties.castTimeIncrease,
                                HEAVY_CAST_TIME);

                penalties.manaCostIncrease =
                        strongest(
                                penalties.manaCostIncrease,
                                HEAVY_MANA_COST);
            }

            case GARB, SHIELD -> {
                /*
                 * Garb never produces an armor proficiency
                 * penalty.
                 *
                 * Shields are resolved separately because
                 * they are held equipment.
                 */
            }
        }
    }

    private static ArmorCategory highestNonProficientArmor(
            Set<ArmorCategory> categories) {

        if (categories == null
                || categories.isEmpty()) {

            return null;
        }

        if (categories.contains(
                ArmorCategory.HEAVY)) {

            return ArmorCategory.HEAVY;
        }

        if (categories.contains(
                ArmorCategory.MEDIUM)) {

            return ArmorCategory.MEDIUM;
        }

        if (categories.contains(
                ArmorCategory.LIGHT)) {

            return ArmorCategory.LIGHT;
        }

        return null;
    }

    private static double strongest(
            double current,
            double candidate) {

        return Math.max(
                current,
                candidate);
    }

    private static final class MutablePenalties {

        private double attackDamageReduction;
        private double attackSpeedReduction;
        private double movementSpeedReduction;

        private double projectileDamageReduction;
        private double drawSpeedReduction;

        private double castTimeIncrease;
        private double manaCostIncrease;
    }
}