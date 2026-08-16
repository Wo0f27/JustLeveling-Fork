package com.seniors.justlevelingfork.common.feat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class FeatDefinition {

    private final ResourceLocation id;
    private final String name;
    private final String description;
    private final boolean repeatable;
    private final int maxRank;
    private final ResourceLocation effectType;
    private final JsonObject effectData;
    private final FeatPrerequisites prerequisites;

    public FeatDefinition(
            ResourceLocation id,
            String name,
            String description,
            boolean repeatable,
            int maxRank,
            ResourceLocation effectType,
            JsonObject effectData,
            FeatPrerequisites prerequisites) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.repeatable = repeatable;
        this.maxRank = Math.max(0, maxRank);
        this.effectType = effectType;

        this.effectData =
                effectData == null
                        ? new JsonObject()
                        : effectData.deepCopy();

        this.prerequisites =
                prerequisites == null
                        ? FeatPrerequisites.empty()
                        : prerequisites;
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
        return prerequisites.minimumCharacterLevel();
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public ResourceLocation getEffectType() {
        return effectType;
    }

    public JsonObject getEffectData() {
        return effectData.deepCopy();
    }

    public FeatPrerequisites getPrerequisites() {
        return prerequisites;
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