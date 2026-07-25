package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.config.TitleConfigService;
import com.seniors.justlevelingfork.config.models.TitleModel;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Client-facing title metadata. Requirements deliberately remain server-side:
 * clients only need an id and display flags to render and select synced titles.
 */
public record TitleDefinitionsSyncPayload(List<Definition> definitions) {
    public static final int MAX_DEFINITIONS = 512;
    public static final int MAX_TITLE_ID_LENGTH = 64;

    public TitleDefinitionsSyncPayload {
        definitions = List.copyOf(definitions == null ? List.of() : definitions);
    }

    public static TitleDefinitionsSyncPayload current() {
        List<Definition> definitions = new ArrayList<>();
        for (TitleModel model : TitleConfigService.titleModels()) {
            if (model == null || model.TitleId == null || model.TitleId.isBlank()
                    || model.TitleId.length() > MAX_TITLE_ID_LENGTH) {
                continue;
            }
            definitions.add(new Definition(
                    model.TitleId,
                    model.Default,
                    Boolean.TRUE.equals(model.HideRequirements)));
            if (definitions.size() == MAX_DEFINITIONS) {
                break;
            }
        }
        return new TitleDefinitionsSyncPayload(definitions);
    }

    public static TitleDefinitionsSyncPayload read(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_DEFINITIONS) {
            throw new IllegalArgumentException("Invalid synced title definition count: " + count);
        }

        List<Definition> definitions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            definitions.add(new Definition(
                    buffer.readUtf(MAX_TITLE_ID_LENGTH),
                    buffer.readBoolean(),
                    buffer.readBoolean()));
        }
        return new TitleDefinitionsSyncPayload(definitions);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(definitions.size());
        for (Definition definition : definitions) {
            buffer.writeUtf(definition.titleId(), MAX_TITLE_ID_LENGTH);
            buffer.writeBoolean(definition.defaultTitle());
            buffer.writeBoolean(definition.hideRequirements());
        }
    }

    public void apply() {
        Collection<TitleModel> models = definitions.stream()
                .map(definition -> new TitleModel(
                        definition.titleId(),
                        List.of(),
                        definition.defaultTitle(),
                        definition.hideRequirements()))
                .toList();
        RegistryTitles.setClientTitleModels(models);
    }

    public record Definition(String titleId, boolean defaultTitle, boolean hideRequirements) {
    }
}
