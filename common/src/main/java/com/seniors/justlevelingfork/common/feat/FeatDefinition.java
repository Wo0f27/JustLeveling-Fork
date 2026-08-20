package com.seniors.justlevelingfork.common.feat;

import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class FeatDefinition {

    private final ResourceLocation id;
    private final String name;
    private final String description;
    private final boolean repeatable;
    private final int maxRank;

    private final List<FeatEffectDefinition> effects;

    private final FeatPrerequisites prerequisites;

    public FeatDefinition(
            ResourceLocation id,
            String name,
            String description,
            boolean repeatable,
            int maxRank,
            List<FeatEffectDefinition> effects,
            FeatPrerequisites prerequisites) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.repeatable = repeatable;
        this.maxRank = Math.max(0, maxRank);

        this.effects =
                List.copyOf(
                        effects == null
                                ? List.of()
                                : effects);

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

    public List<FeatEffectDefinition> getEffects() {
        return effects;
    }

    public boolean hasEffectType(
            ResourceLocation effectType) {

        if (effectType == null) {
            return false;
        }

        return effects.stream()
                .anyMatch(effect ->
                        effectType.equals(
                                effect.getType()));
    }

    /*
     * Compatibility helpers while the rest of JLF
     * transitions from one effect to multiple effects.
     */
    public ResourceLocation getEffectType() {

        return effects.isEmpty()
                ? null
                : effects.get(0).getType();
    }

    public JsonObject getEffectData() {

        return effects.isEmpty()
                ? new JsonObject()
                : effects.get(0).getData();
    }

    public FeatPrerequisites getPrerequisites() {
        return prerequisites;
    }

    public boolean canGainRank(
            int currentRank) {

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