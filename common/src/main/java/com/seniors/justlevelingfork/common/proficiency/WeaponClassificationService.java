package com.seniors.justlevelingfork.common.proficiency;

import com.seniors.justlevelingfork.registry.RegistryWeaponProficiencies;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WeaponClassificationService {

    private WeaponClassificationService() {
    }

    /**
     * Returns all built-in JLF weapon proficiency tags
     * assigned to this item.
     *
     * An item may intentionally have several classifications.
     *
     * Example:
     *
     * minecraft:bow
     * -> simple
     * -> martial
     * -> shortbow
     * -> longbow
     *
     * This is useful because vanilla Minecraft does not
     * distinguish shortbows from longbows.
     */
    public static Set<ResourceLocation> getClassifications(
            ItemStack stack) {

        if (!valid(stack)) {
            return Set.of();
        }

        Set<ResourceLocation> result =
                new LinkedHashSet<>();

        for (ResourceLocation proficiencyId :
                RegistryWeaponProficiencies.values()) {

            if (matches(
                    stack,
                    proficiencyId)) {

                result.add(
                        proficiencyId);
            }
        }

        if (result.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                result);
    }

    /**
     * Checks an arbitrary namespaced proficiency tag.
     *
     * This is deliberately NOT limited to JLF's built-in
     * proficiency list.
     *
     * An addon could therefore use:
     *
     * mymod:weapons/types/katana
     *
     * without modifying JLF.
     */
    public static boolean matches(
            ItemStack stack,
            ResourceLocation proficiencyId) {

        if (!valid(stack)
                || proficiencyId == null) {
            return false;
        }

        TagKey<Item> tag =
                RegistryWeaponProficiencies.tag(
                        proficiencyId);

        return tag != null
                && stack.is(tag);
    }

    public static boolean isSimple(
            ItemStack stack) {

        return matches(
                stack,
                RegistryWeaponProficiencies.SIMPLE);
    }

    public static boolean isMartial(
            ItemStack stack) {

        return matches(
                stack,
                RegistryWeaponProficiencies.MARTIAL);
    }

    public static boolean isClassifiedWeapon(
            ItemStack stack) {

        return !getClassifications(stack)
                .isEmpty();
    }

    private static boolean valid(
            ItemStack stack) {

        return stack != null
                && !stack.isEmpty();
    }
}