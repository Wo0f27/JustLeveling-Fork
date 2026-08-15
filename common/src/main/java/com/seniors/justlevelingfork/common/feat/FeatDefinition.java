package com.seniors.justlevelingfork.common.feat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class FeatDefinition {

    private final ResourceLocation id;
    private final String name;
    private final String description;
    private final int minimumCharacterLevel;
    private final boolean repeatable;
    private final int maxRank;
    private final ResourceLocation effectType;
    private final JsonObject effectData;

    public FeatDefinition(
            ResourceLocation id,
            String name,
            String description,
            int minimumCharacterLevel,
            boolean repeatable,
            int maxRank,
            ResourceLocation effectType,
            JsonObject effectData) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.minimumCharacterLevel =
                Math.max(1, minimumCharacterLevel);
        this.repeatable = repeatable;
        this.maxRank =
                Math.max(0, maxRank);
        this.effectType = effectType;

        /*
         * Keep our own copy so callers cannot mutate
         * the original parsed JSON object.
         */
        this.effectData =
                effectData == null
                        ? new JsonObject()
                        : effectData.deepCopy();
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMinimumCharacterLevel() {
        return minimumCharacterLevel;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    /*
     * 0 = unlimited ranks.
     */
    public int getMaxRank() {
        return maxRank;
    }

    public ResourceLocation getEffectType() {
        return effectType;
    }

    public JsonObject getEffectData() {
        return effectData.deepCopy();
    }

    public boolean canGainRank(int currentRank) {

        if (currentRank <= 0) {
            return true;
        }

        if (!repeatable) {
            return false;
        }

        return maxRank <= 0
                || currentRank < maxRank;
    }
}