package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.feat.FeatClientState;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.feat.FeatEffectDefinition;
import com.seniors.justlevelingfork.common.feat.FeatManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record FeatDefinitionsSyncPayload(
        List<Definition> definitions) {

    private static final int MAX_DEFINITIONS = 512;

    private static final int MAX_ID_LENGTH = 256;
    private static final int MAX_NAME_LENGTH = 128;
    private static final int MAX_DESCRIPTION_LENGTH = 1024;
    private static final int MAX_EFFECTS = 32;
    private static final int MAX_REQUIREMENTS = 64;

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
                    || feat.getEffects().isEmpty()) {

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
                            feat.getEffects()
                                    .stream()
                                    .map(
                                            FeatEffectDefinition::getType)
                                    .toList(),
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

            writeEffectTypes(
                    buffer,
                    definition.effectTypes());

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

    private static void writeEffectTypes(
            FriendlyByteBuf buffer,
            List<ResourceLocation> effectTypes) {

        buffer.writeVarInt(
                effectTypes.size());

        effectTypes.forEach(effectType ->
                buffer.writeUtf(
                        effectType.toString(),
                        MAX_ID_LENGTH));
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

            List<ResourceLocation> effectTypes =
                    readEffectTypes(
                            buffer);

            Map<String, Integer> abilityRequirements =
                    readStringRequirements(
                            buffer);

            Map<ResourceLocation, Integer> classRequirements =
                    readResourceRequirements(
                            buffer);

            Map<ResourceLocation, Integer> featRequirements =
                    readResourceRequirements(
                            buffer);

            if (id == null) {

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
                            effectTypes,
                            abilityRequirements,
                            classRequirements,
                            featRequirements));
        }

        return new FeatDefinitionsSyncPayload(
                definitions);
    }

    private static List<ResourceLocation> readEffectTypes(
            FriendlyByteBuf buffer) {

        int count =
                buffer.readVarInt();

        if (count <= 0
                || count > MAX_EFFECTS) {

            throw new IllegalArgumentException(
                    "Invalid synced feat effect count: "
                            + count);
        }

        List<ResourceLocation> result =
                new ArrayList<>(
                        count);

        for (int i = 0;
             i < count;
             i++) {

            ResourceLocation id =
                    ResourceLocation.tryParse(
                            buffer.readUtf(
                                    MAX_ID_LENGTH));

            if (id == null) {

                throw new IllegalArgumentException(
                        "Invalid synced feat effect id.");
            }

            result.add(
                    id);
        }

        return List.copyOf(
                result);
    }

    private static Map<String, Integer> readStringRequirements(
            FriendlyByteBuf buffer) {

        int count =
                buffer.readVarInt();

        if (count < 0
                || count > MAX_REQUIREMENTS) {

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

        if (count < 0
                || count > MAX_REQUIREMENTS) {

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
            List<ResourceLocation> effectTypes,
            Map<String, Integer> abilityRequirements,
            Map<ResourceLocation, Integer> classRequirements,
            Map<ResourceLocation, Integer> featRequirements) {

        public Definition {

            effectTypes =
                    List.copyOf(
                            effectTypes == null
                                    ? List.of()
                                    : effectTypes);

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
