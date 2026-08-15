package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.feat.FeatClientState;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.feat.FeatManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record FeatDefinitionsSyncPayload(
        List<Definition> definitions) {

    private static final int MAX_DEFINITIONS = 512;

    private static final int MAX_ID_LENGTH = 256;
    private static final int MAX_NAME_LENGTH = 128;
    private static final int MAX_DESCRIPTION_LENGTH = 1024;

    public FeatDefinitionsSyncPayload {

        definitions =
                List.copyOf(
                        definitions == null
                                ? List.of()
                                : definitions);
    }

    /*
     * Build a client-facing snapshot from the
     * currently loaded server datapack definitions.
     */
    public static FeatDefinitionsSyncPayload current() {

        List<Definition> result =
                new ArrayList<>();

        for (FeatDefinition feat
                : FeatManager.INSTANCE.values()) {

            if (feat == null
                    || feat.getId() == null
                    || feat.getEffectType() == null) {
                continue;
            }

            result.add(
                    new Definition(
                            feat.getId(),
                            feat.getName(),
                            feat.getDescription(),
                            feat.getMinimumCharacterLevel(),
                            feat.isRepeatable(),
                            feat.getMaxRank(),
                            feat.getEffectType()));

            if (result.size() >= MAX_DEFINITIONS) {
                break;
            }
        }

        return new FeatDefinitionsSyncPayload(
                result);
    }

    public void write(
            FriendlyByteBuf buffer) {

        buffer.writeVarInt(
                definitions.size());

        for (Definition definition
                : definitions) {

            buffer.writeUtf(
                    definition.id().toString(),
                    MAX_ID_LENGTH);

            buffer.writeUtf(
                    safeString(
                            definition.name()),
                    MAX_NAME_LENGTH);

            buffer.writeUtf(
                    safeString(
                            definition.description()),
                    MAX_DESCRIPTION_LENGTH);

            buffer.writeVarInt(
                    definition.minimumCharacterLevel());

            buffer.writeBoolean(
                    definition.repeatable());

            buffer.writeVarInt(
                    definition.maxRank());

            buffer.writeUtf(
                    definition.effectType().toString(),
                    MAX_ID_LENGTH);
        }
    }

    public static FeatDefinitionsSyncPayload read(
            FriendlyByteBuf buffer) {

        int count =
                buffer.readVarInt();

        if (count < 0
                || count > MAX_DEFINITIONS) {

            throw new IllegalArgumentException(
                    "Invalid synced feat definition count: "
                            + count);
        }

        List<Definition> definitions =
                new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            ResourceLocation id =
                    ResourceLocation.tryParse(
                            buffer.readUtf(
                                    MAX_ID_LENGTH));

            String name =
                    buffer.readUtf(
                            MAX_NAME_LENGTH);

            String description =
                    buffer.readUtf(
                            MAX_DESCRIPTION_LENGTH);

            int minimumCharacterLevel =
                    buffer.readVarInt();

            boolean repeatable =
                    buffer.readBoolean();

            int maxRank =
                    buffer.readVarInt();

            ResourceLocation effectType =
                    ResourceLocation.tryParse(
                            buffer.readUtf(
                                    MAX_ID_LENGTH));

            if (id == null
                    || effectType == null) {

                throw new IllegalArgumentException(
                        "Invalid synced feat definition.");
            }

            definitions.add(
                    new Definition(
                            id,
                            name,
                            description,
                            minimumCharacterLevel,
                            repeatable,
                            maxRank,
                            effectType));
        }

        return new FeatDefinitionsSyncPayload(
                definitions);
    }

    /*
     * Runs client-side after receiving the packet.
     */
    public void apply() {

        FeatClientState.replace(
                definitions);
    }

    private static String safeString(
            String value) {

        return value == null
                ? ""
                : value;
    }

    public record Definition(
            ResourceLocation id,
            String name,
            String description,
            int minimumCharacterLevel,
            boolean repeatable,
            int maxRank,
            ResourceLocation effectType) {
    }
}