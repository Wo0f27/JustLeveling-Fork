package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public record FeatPrerequisites(
        int minimumCharacterLevel,
        Map<String, Integer> abilities,
        Map<ResourceLocation, Integer> classes,
        Map<ResourceLocation, Integer> feats,
        Set<ArmorCategory> armorProficiencies) {

    public FeatPrerequisites {

        minimumCharacterLevel =
                Math.max(
                        1,
                        minimumCharacterLevel);

        abilities =
                immutableRequirements(
                        abilities);

        classes =
                immutableRequirements(
                        classes);

        feats =
                immutableRequirements(
                        feats);

        armorProficiencies =
                immutableArmorProficiencies(
                        armorProficiencies);
    }

    public static FeatPrerequisites empty() {

        return new FeatPrerequisites(
                1,
                Map.of(),
                Map.of(),
                Map.of(),
                Set.of());
    }

    private static <K> Map<K, Integer>
    immutableRequirements(
            Map<K, Integer> source) {

        if (source == null
                || source.isEmpty()) {

            return Map.of();
        }

        Map<K, Integer> result =
                new LinkedHashMap<>();

        source.forEach((key, value) -> {

            if (key == null
                    || value == null
                    || value <= 0) {

                return;
            }

            result.put(
                    key,
                    value);
        });

        return Collections.unmodifiableMap(
                result);
    }

    private static Set<ArmorCategory>
    immutableArmorProficiencies(
            Set<ArmorCategory> source) {

        if (source == null
                || source.isEmpty()) {

            return Set.of();
        }

        EnumSet<ArmorCategory> result =
                EnumSet.noneOf(
                        ArmorCategory.class);

        for (ArmorCategory category : source) {

            if (category != null) {
                result.add(category);
            }
        }

        if (result.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                result);
    }
}