package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public record CharacterClassDefinition(
        ResourceLocation id,
        Map<Aptitude, Integer> recommendedAbilityScores,
        ClassAbilityRequirement multiclassRequirement,
        Set<ArmorCategory> startingArmorProficiencies,
        Set<ArmorCategory> multiclassArmorProficiencies,
        Set<ResourceLocation> startingWeaponProficiencies,
        Set<ResourceLocation> multiclassWeaponProficiencies) {

    public CharacterClassDefinition {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Character class id cannot be null.");
        }

        recommendedAbilityScores =
                Map.copyOf(
                        recommendedAbilityScores == null
                                ? Map.of()
                                : recommendedAbilityScores);

        multiclassRequirement =
                multiclassRequirement == null
                        ? ClassAbilityRequirement.none()
                        : multiclassRequirement;

        startingArmorProficiencies =
                Set.copyOf(
                        startingArmorProficiencies == null
                                ? Set.of()
                                : startingArmorProficiencies);

        multiclassArmorProficiencies =
                Set.copyOf(
                        multiclassArmorProficiencies == null
                                ? Set.of()
                                : multiclassArmorProficiencies);

        startingWeaponProficiencies =
                Set.copyOf(
                        startingWeaponProficiencies == null
                                ? Set.of()
                                : startingWeaponProficiencies);

        multiclassWeaponProficiencies =
                Set.copyOf(
                        multiclassWeaponProficiencies == null
                                ? Set.of()
                                : multiclassWeaponProficiencies);
    }
}