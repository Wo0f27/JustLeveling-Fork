package com.seniors.justlevelingfork.common.feat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class FeatEffectDefinition {

    private final ResourceLocation type;
    private final JsonObject data;

    public FeatEffectDefinition(
            ResourceLocation type,
            JsonObject data) {

        this.type = type;

        this.data =
                data == null
                        ? new JsonObject()
                        : data.deepCopy();
    }

    public ResourceLocation getType() {
        return type;
    }

    public JsonObject getData() {
        return data.deepCopy();
    }
}