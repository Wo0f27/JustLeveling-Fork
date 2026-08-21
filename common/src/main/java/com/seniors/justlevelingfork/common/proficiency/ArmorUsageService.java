package com.seniors.justlevelingfork.common.proficiency;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ArmorUsageService {

    private ArmorUsageService() {
    }
    /**
     * Returns all classified worn-equipment categories in
     * the player's normal Minecraft armor slots.
     *
     * This includes Garb, Light, Medium and Heavy.
     *
     * Shields are intentionally excluded because they are
     * handled separately as held equipment.
     */
    public static Set<ArmorCategory> getWornArmorCategories(
            ServerPlayer player) {

        if (player == null) {
            return Set.of();
        }

        EnumSet<ArmorCategory> result =
                EnumSet.noneOf(
                        ArmorCategory.class);

        for (ItemStack stack : player.getArmorSlots()) {

            if (stack == null || stack.isEmpty()) {
                continue;
            }

            for (ArmorCategory category :
                    ArmorClassificationService
                            .getCategories(stack)) {

                if (category != ArmorCategory.SHIELD) {
                    result.add(category);
                }
            }
        }

        return immutable(result);
    }

    public static boolean isWearingAnyArmor(
            ServerPlayer player) {

        Set<ArmorCategory> categories =
                getWornArmorCategories(player);

        return categories.contains(
                ArmorCategory.LIGHT)
                || categories.contains(
                ArmorCategory.MEDIUM)
                || categories.contains(
                ArmorCategory.HEAVY);
    }

    public static boolean isWearingGarb(
            ServerPlayer player) {

        return isWearingArmorCategory(
                player,
                ArmorCategory.GARB);
    }

    public static boolean isWearingArmorCategory(
            ServerPlayer player,
            ArmorCategory category) {

        if (player == null
                || category == null
                || category == ArmorCategory.SHIELD) {
            return false;
        }

        return getWornArmorCategories(player)
                .contains(category);
    }

    /**
     * Returns the armor categories currently worn for which
     * the player lacks proficiency.
     *
     * This reports the individual missing categories rather
     * than simply marking the entire item as invalid.
     */
    public static Set<ArmorCategory>
    getNonProficientWornArmorCategories(
            ServerPlayer player) {

        if (player == null) {
            return Set.of();
        }

        EnumSet<ArmorCategory> result =
                EnumSet.noneOf(
                        ArmorCategory.class);

        for (ItemStack stack : player.getArmorSlots()) {

            if (stack == null || stack.isEmpty()) {
                continue;
            }

            Set<ArmorCategory> categories =
                    ArmorClassificationService
                            .getCategories(stack);

            for (ArmorCategory category : categories) {

                if (category == ArmorCategory.SHIELD
                        || category == ArmorCategory.GARB) {
                    continue;
                }

                if (!ArmorProficiencyService
                        .hasProficiency(
                                player,
                                category)) {

                    result.add(category);
                }
            }
        }

        return immutable(result);
    }

    public static boolean isWearingNonProficientArmor(
            ServerPlayer player) {

        return !getNonProficientWornArmorCategories(
                player)
                .isEmpty();
    }

    /**
     * A shield is considered "in use" when a classified
     * shield is held in either hand.
     */
    public static boolean isUsingShield(
            ServerPlayer player) {

        if (player == null) {
            return false;
        }

        return ArmorClassificationService.isShield(
                player.getMainHandItem())
                || ArmorClassificationService.isShield(
                player.getOffhandItem());
    }

    public static boolean isUsingNonProficientShield(
            ServerPlayer player) {

        return isUsingShield(player)
                && !ArmorProficiencyService
                .hasProficiency(
                        player,
                        ArmorCategory.SHIELD);
    }

    private static Set<ArmorCategory> immutable(
            EnumSet<ArmorCategory> categories) {

        if (categories == null
                || categories.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                EnumSet.copyOf(categories));
    }
}