package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.feat.FeatClientState;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.feat.FeatManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

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
                            feat.getEffectType(),
                            feat.getPrerequisites().abilities(),
                            feat.getPrerequisites().classes(),
                            feat.getPrerequisites().feats()));

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
            writeStringRequirements(
                    buffer,
                    definition.abilityRequirements());

            writeResourceRequirements(
                    buffer,
                    definition.classRequirements());

            writeResourceRequirements(
                    buffer,
                    definition.featRequirements());
        }
    }

    private static void writeStringRequirements(
            FriendlyByteBuf buffer,
            Map<String, Integer> requirements) {

        buffer.writeVarInt(
                requirements.size());

        requirements.forEach((id, value) -> {

            buffer.writeUtf(
                    id,
                    MAX_ID_LENGTH);

            buffer.writeVarInt(
                    value);
        });
    }

    private static void writeResourceRequirements(
            FriendlyByteBuf buffer,
            Map<ResourceLocation, Integer> requirements) {

        buffer.writeVarInt(
                requirements.size());

        requirements.forEach((id, value) -> {

            buffer.writeUtf(
                    id.toString(),
                    MAX_ID_LENGTH);

            buffer.writeVarInt(
                    value);
        });
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
            Map<String, Integer> abilityRequirements =
                    readStringRequirements(buffer);

            Map<ResourceLocation, Integer> classRequirements =
                    readResourceRequirements(buffer);

            Map<ResourceLocation, Integer> featRequirements =
                    readResourceRequirements(buffer);


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
                            effectType,
                            abilityRequirements,
                            classRequirements,
                            featRequirements));
        }

        return new FeatDefinitionsSyncPayload(
                definitions);
    }

    private static Map<String, Integer> readStringRequirements(
            FriendlyByteBuf buffer) {

        int count =
                buffer.readVarInt();

        if (count < 0 || count > 64) {
            throw new IllegalArgumentException(
                    "Invalid feat prerequisite count: "
                            + count);
        }

        Map<String, Integer> result =
                new java.util.LinkedHashMap<>();

        for (int i = 0; i < count; i++) {

            String id =
                    buffer.readUtf(
                            MAX_ID_LENGTH);

            int value =
                    buffer.readVarInt();

            result.put(
                    id,
                    value);
        }

        return result;
    }

    private static Map<ResourceLocation, Integer> readResourceRequirements(
            FriendlyByteBuf buffer) {

        int count =
                buffer.readVarInt();

        if (count < 0 || count > 64) {
            throw new IllegalArgumentException(
                    "Invalid feat prerequisite count: "
                            + count);
        }

        Map<ResourceLocation, Integer> result =
                new java.util.LinkedHashMap<>();

        for (int i = 0; i < count; i++) {

            ResourceLocation id =
                    ResourceLocation.tryParse(
                            buffer.readUtf(
                                    MAX_ID_LENGTH));

            int value =
                    buffer.readVarInt();

            if (id == null) {
                throw new IllegalArgumentException(
                        "Invalid synced prerequisite id.");
            }

            result.put(
                    id,
                    value);
        }

        return result;
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
            ResourceLocation effectType,
            Map<String, Integer> abilityRequirements,
            Map<ResourceLocation, Integer> classRequirements,
            Map<ResourceLocation, Integer> featRequirements) {

        public Definition {

            abilityRequirements =
                    Map.copyOf(
                            abilityRequirements == null
                                    ? Map.of()
                                    : abilityRequirements);

            classRequirements =
                    Map.copyOf(
                            classRequirements == null
                                    ? Map.of()
                                    : classRequirements);

            featRequirements =
                    Map.copyOf(
                            featRequirements == null
                                    ? Map.of()
                                    : featRequirements);
        }
    }
}