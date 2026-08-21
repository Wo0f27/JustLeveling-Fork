package com.seniors.justlevelingfork.common.proficiency;

import com.seniors.justlevelingfork.registry.RegistryTags;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public final class ArmorClassificationService {

    private ArmorClassificationService() {
    }

    public static boolean isGarb(ItemStack stack) {
        return valid(stack)
                && stack.is(
                RegistryTags.Items.ARMOR_GARB);
    }

    public static boolean isLight(ItemStack stack) {
        return valid(stack)
                && stack.is(
                RegistryTags.Items.ARMOR_LIGHT);
    }

    public static boolean isMedium(ItemStack stack) {
        return valid(stack)
                && stack.is(
                RegistryTags.Items.ARMOR_MEDIUM);
    }

    public static boolean isHeavy(ItemStack stack) {
        return valid(stack)
                && stack.is(
                RegistryTags.Items.ARMOR_HEAVY);
    }

    public static boolean isShield(ItemStack stack) {
        return valid(stack)
                && stack.is(
                RegistryTags.Items.SHIELDS);
    }

    /**
     * Returns every JLF worn-equipment category assigned
     * to this item.
     *
     * Normally a worn item should have exactly one of
     * GARB, LIGHT, MEDIUM or HEAVY.
     *
     * Shields are classified separately but returned by
     * this general classification method when applicable.
     */
    public static Set<ArmorCategory> getCategories(
            ItemStack stack) {

        if (!valid(stack)) {
            return Set.of();
        }

        EnumSet<ArmorCategory> categories =
                EnumSet.noneOf(
                        ArmorCategory.class);
        if (isGarb(stack)) {
            categories.add(
                    ArmorCategory.GARB);
        }

        if (isLight(stack)) {
            categories.add(
                    ArmorCategory.LIGHT);
        }

        if (isMedium(stack)) {
            categories.add(
                    ArmorCategory.MEDIUM);
        }

        if (isHeavy(stack)) {
            categories.add(
                    ArmorCategory.HEAVY);
        }

        if (isShield(stack)) {
            categories.add(
                    ArmorCategory.SHIELD);
        }

        if (categories.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                categories);
    }

    public static boolean isClassified(
            ItemStack stack) {

        return !getCategories(stack)
                .isEmpty();
    }

    public static boolean isArmor(
            ItemStack stack) {

        return isLight(stack)
                || isMedium(stack)
                || isHeavy(stack);
    }

    private static boolean valid(ItemStack stack) {
        return stack != null
                && !stack.isEmpty();
    }
}