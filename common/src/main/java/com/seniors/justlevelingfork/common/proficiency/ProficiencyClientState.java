package com.seniors.justlevelingfork.common.proficiency;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ProficiencyClientState {

    private static Set<ResourceLocation> weaponProficiencies =
            Set.of();

    private static Set<ArmorCategory> armorProficiencies =
            Set.of();

    private static boolean synchronizedState;

    private ProficiencyClientState() {
    }

    public static void replace(
            Set<ResourceLocation> weapons,
            Set<ArmorCategory> armor) {

        LinkedHashSet<ResourceLocation> updatedWeapons =
                new LinkedHashSet<>();

        if (weapons != null) {

            for (ResourceLocation id : weapons) {

                if (id != null) {
                    updatedWeapons.add(id);
                }
            }
        }

        weaponProficiencies =
                updatedWeapons.isEmpty()
                        ? Set.of()
                        : Collections.unmodifiableSet(
                        updatedWeapons);

        EnumSet<ArmorCategory> updatedArmor =
                EnumSet.noneOf(
                        ArmorCategory.class);

        if (armor != null) {

            for (ArmorCategory category : armor) {

                if (category != null) {
                    updatedArmor.add(category);
                }
            }
        }

        armorProficiencies =
                updatedArmor.isEmpty()
                        ? Set.of()
                        : Collections.unmodifiableSet(
                        updatedArmor);

        synchronizedState = true;
    }

    public static Set<ResourceLocation> weaponProficiencies() {
        return weaponProficiencies;
    }

    public static Set<ArmorCategory> armorProficiencies() {
        return armorProficiencies;
    }

    public static boolean hasWeaponProficiency(
            ResourceLocation id) {

        return id != null
                && weaponProficiencies.contains(id);
    }

    public static boolean hasArmorProficiency(
            ArmorCategory category) {

        return category != null
                && armorProficiencies.contains(category);
    }

    public static void clear() {

        weaponProficiencies =
                Set.of();

        armorProficiencies =
                Set.of();

        synchronizedState = false;
    }
    public static boolean isSynchronized() {
        return synchronizedState;
    }
}