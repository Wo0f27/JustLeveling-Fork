package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public record CharacterClassDefinition(
        ResourceLocation id,
        Map<Aptitude, Integer> recommendedAbilityScores,
        ClassAbilityRequirement multiclassRequirement,
        Set<ArmorCategory> startingArmorProficiencies,
        Set<ArmorCategory> multiclassArmorProficiencies) {

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
    }
}