package com.seniors.justlevelingfork.common.feat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record FeatPrerequisites(
        int minimumCharacterLevel,
        Map<String, Integer> abilities,
        Map<ResourceLocation, Integer> classes,
        Map<ResourceLocation, Integer> feats) {

    public FeatPrerequisites {

        minimumCharacterLevel =
                Math.max(1, minimumCharacterLevel);

        abilities =
                immutableRequirements(abilities);

        classes =
                immutableRequirements(classes);

        feats =
                immutableRequirements(feats);
    }

    public static FeatPrerequisites empty() {
        return new FeatPrerequisites(
                1,
                Map.of(),
                Map.of(),
                Map.of());
    }

    private static <K> Map<K, Integer> immutableRequirements(
            Map<K, Integer> source) {

        if (source == null || source.isEmpty()) {
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

            result.put(key, value);
        });

        return Collections.unmodifiableMap(result);
    }
}