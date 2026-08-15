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

        int minimumCharacterLevel =
                GsonHelper.getAsInt(
                        json,
                        "minimum_character_level",
                        1);

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

        JsonObject effect =
                GsonHelper.getAsJsonObject(
                        json,
                        "effect");

        ResourceLocation effectType =
                ResourceLocation.tryParse(
                        GsonHelper.getAsString(
                                effect,
                                "type"));

        if (effectType == null) {
            throw new IllegalArgumentException(
                    "Invalid feat effect type for "
                            + id);
        }

        /*
         * Keep the entire effect object.
         *
         * The appropriate Java effect handler will
         * interpret the remaining fields later.
         */
        return new FeatDefinition(
                id,
                name,
                description,
                minimumCharacterLevel,
                repeatable,
                maxRank,
                effectType,
                effect);
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