package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.network.FeatDefinitionsSyncPayload;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class FeatClientState {

    private static Map<ResourceLocation, FeatDefinitionsSyncPayload.Definition>
            definitions = Map.of();

    private FeatClientState() {
    }

    public static void replace(
            Collection<FeatDefinitionsSyncPayload.Definition> newDefinitions) {

        Map<ResourceLocation, FeatDefinitionsSyncPayload.Definition> updated =
                new LinkedHashMap<>();

        if (newDefinitions != null) {
            for (FeatDefinitionsSyncPayload.Definition definition
                    : newDefinitions) {

                if (definition == null
                        || definition.id() == null) {
                    continue;
                }

                updated.put(
                        definition.id(),
                        definition);
            }
        }

        definitions =
                Map.copyOf(updated);

        Constants.LOG.info(
                "Synced {} feat definitions to client.",
                definitions.size());
    }

    public static FeatDefinitionsSyncPayload.Definition get(
            ResourceLocation id) {

        if (id == null) {
            return null;
        }

        return definitions.get(id);
    }

    public static Collection<FeatDefinitionsSyncPayload.Definition> values() {
        return definitions.values();
    }

    public static int size() {
        return definitions.size();
    }

    public static void clear() {
        definitions = Map.of();
    }
}