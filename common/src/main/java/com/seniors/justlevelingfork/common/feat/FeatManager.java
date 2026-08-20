package com.seniors.justlevelingfork.common.feat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.Constants;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import java.util.Locale;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;

public final class FeatManager
        extends SimpleJsonResourceReloadListener {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    public static final FeatManager INSTANCE =
            new FeatManager();

    /*
     * Replaced atomically after every datapack reload.
     */
    private Map<ResourceLocation, FeatDefinition> feats =
            Map.of();

    private FeatManager() {

        /*
         * Loads:
         *
         * data/<namespace>/feats/*.json
         */
        super(GSON, "feats");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> jsonEntries,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {

        Map<ResourceLocation, FeatDefinition> loaded =
                new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry
                : jsonEntries.entrySet()) {

            ResourceLocation id =
                    entry.getKey();

            try {

                JsonObject json =
                        GsonHelper.convertToJsonObject(
                                entry.getValue(),
                                "feat");

                FeatDefinition definition =
                        parse(id, json);

                loaded.put(
                        id,
                        definition);

            } catch (Exception exception) {

                Constants.LOG.error(
                        "Failed to load feat definition {}",
                        id,
                        exception);
            }
        }

        feats =
                Collections.unmodifiableMap(loaded);

        Constants.LOG.info(
                "Loaded {} feat definitions.",
                feats.size());
    }

    private FeatDefinition parse(
            ResourceLocation id,
            JsonObject json) {

        String name =
                GsonHelper.getAsString(
                        json,
                        "name",
                        id.getPath());

        String description =
                GsonHelper.getAsString(
                        json,
                        "description",
                        "");

        boolean repeatable =
                GsonHelper.getAsBoolean(
                        json,
                        "repeatable",
                        false);

        int maxRank =
                GsonHelper.getAsInt(
                        json,
                        "max_rank",
                        repeatable ? 0 : 1);

        List<FeatEffectDefinition> effects =
                parseEffects(
                        id,
                        json);

        FeatPrerequisites prerequisites =
                parsePrerequisites(
                        id,
                        json);

        return new FeatDefinition(
                id,
                name,
                description,
                repeatable,
                maxRank,
                effects,
                prerequisites);
    }

    private List<FeatEffectDefinition> parseEffects(
            ResourceLocation featId,
            JsonObject json) {

        /*
         * New format:
         *
         * "effects": [
         *   { ... },
         *   { ... }
         * ]
         */
        if (json.has("effects")) {

            if (!json.get("effects").isJsonArray()) {

                throw new IllegalArgumentException(
                        "'effects' must be an array in feat "
                                + featId);
            }

            JsonArray array =
                    json.getAsJsonArray(
                            "effects");

            if (array.isEmpty()) {

                throw new IllegalArgumentException(
                        "Feat must contain at least one effect: "
                                + featId);
            }

            List<FeatEffectDefinition> effects =
                    new ArrayList<>();

            for (JsonElement element : array) {

                if (!element.isJsonObject()) {

                    throw new IllegalArgumentException(
                            "Feat effect must be an object in "
                                    + featId);
                }

                effects.add(
                        parseEffect(
                                featId,
                                element.getAsJsonObject()));
            }

            return List.copyOf(
                    effects);
        }

        /*
         * Legacy format:
         *
         * "effect": {
         *   ...
         * }
         *
         * Existing datapacks remain valid.
         */
        if (json.has("effect")
                && json.get("effect").isJsonObject()) {

            return List.of(
                    parseEffect(
                            featId,
                            json.getAsJsonObject(
                                    "effect")));
        }

        throw new IllegalArgumentException(
                "Feat has no effect or effects: "
                        + featId);
    }

    private FeatEffectDefinition parseEffect(
            ResourceLocation featId,
            JsonObject effect) {

        ResourceLocation effectType =
                ResourceLocation.tryParse(
                        GsonHelper.getAsString(
                                effect,
                                "type"));

        if (effectType == null) {

            throw new IllegalArgumentException(
                    "Invalid feat effect type for "
                            + featId);
        }

        return new FeatEffectDefinition(
                effectType,
                effect);
    }

    private FeatPrerequisites parsePrerequisites(
            ResourceLocation featId,
            JsonObject json) {

        JsonObject prerequisites;

        if (json.has("prerequisites")
                && json.get("prerequisites").isJsonObject()) {

            prerequisites =
                    json.getAsJsonObject(
                            "prerequisites");

        } else {

            prerequisites =
                    new JsonObject();
        }

        /*
         * Backwards compatibility:
         *
         * Old feat JSON can still use:
         * "minimum_character_level": 2
         *
         * New JSON should put it inside:
         * "prerequisites".
         */
        int minimumCharacterLevel;

        if (prerequisites.has(
                "minimum_character_level")) {

            minimumCharacterLevel =
                    GsonHelper.getAsInt(
                            prerequisites,
                            "minimum_character_level",
                            1);

        } else {

            minimumCharacterLevel =
                    GsonHelper.getAsInt(
                            json,
                            "minimum_character_level",
                            1);
        }

        Map<String, Integer> abilities =
                parseAbilityRequirements(
                        featId,
                        prerequisites);

        Map<ResourceLocation, Integer> classes =
                parseResourceRequirements(
                        prerequisites,
                        "classes");

        Map<ResourceLocation, Integer> feats =
                parseResourceRequirements(
                        prerequisites,
                        "feats");

        return new FeatPrerequisites(
                minimumCharacterLevel,
                abilities,
                classes,
                feats);
    }

    private Map<String, Integer> parseAbilityRequirements(
            ResourceLocation featId,
            JsonObject prerequisites) {

        if (!prerequisites.has("abilities")
                || !prerequisites
                .get("abilities")
                .isJsonObject()) {

            return Map.of();
        }

        Map<String, Integer> result =
                new LinkedHashMap<>();

        JsonObject abilities =
                prerequisites.getAsJsonObject(
                        "abilities");

        abilities.entrySet().forEach(entry -> {

            String aptitudeName =
                    entry.getKey()
                            .toLowerCase(Locale.ROOT);

            if (RegistryAptitudes.getAptitude(
                    aptitudeName) == null) {

                throw new IllegalArgumentException(
                        "Unknown ability '"
                                + entry.getKey()
                                + "' in feat "
                                + featId);
            }

            int requiredScore =
                    entry.getValue()
                            .getAsInt();

            if (requiredScore <= 0) {

                throw new IllegalArgumentException(
                        "Ability requirement must be positive in feat "
                                + featId);
            }

            result.put(
                    aptitudeName,
                    requiredScore);
        });

        return result;
    }

    private Map<ResourceLocation, Integer> parseResourceRequirements(
            JsonObject prerequisites,
            String key) {

        if (!prerequisites.has(key)
                || !prerequisites
                .get(key)
                .isJsonObject()) {

            return Map.of();
        }

        Map<ResourceLocation, Integer> result =
                new LinkedHashMap<>();

        JsonObject object =
                prerequisites.getAsJsonObject(key);

        object.entrySet().forEach(entry -> {

            String rawId =
                    entry.getKey();

            if (!rawId.contains(":")) {
                rawId =
                        Constants.MOD_ID
                                + ":"
                                + rawId;
            }

            ResourceLocation id =
                    ResourceLocation.tryParse(
                            rawId);

            if (id == null) {
                throw new IllegalArgumentException(
                        "Invalid requirement id: "
                                + entry.getKey());
            }

            int required =
                    entry.getValue()
                            .getAsInt();

            if (required <= 0) {
                throw new IllegalArgumentException(
                        "Requirement value must be positive for "
                                + id);
            }

            result.put(
                    id,
                    required);
        });

        return result;
    }

    public FeatDefinition get(ResourceLocation id) {

        if (id == null) {
            return null;
        }

        return feats.get(id);
    }

    public FeatDefinition get(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        ResourceLocation id =
                ResourceLocation.tryParse(value);

        if (id == null) {
            return null;
        }

        return get(id);
    }

    public Collection<FeatDefinition> values() {
        return feats.values();
    }

    public int size() {
        return feats.size();
    }
}