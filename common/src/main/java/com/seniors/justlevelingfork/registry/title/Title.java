package com.seniors.justlevelingfork.registry.title;

import com.seniors.justlevelingfork.common.config.ClientConfigService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class Title {
    private final ResourceLocation key;
    public final boolean Requirement;
    public final boolean HideRequirements;

    public Title(ResourceLocation key, boolean requirement, boolean hideRequirements) {
        this.key = key;
        this.Requirement = requirement;
        this.HideRequirements = hideRequirements;
    }

    public Title get() {
        return this;
    }

    public ResourceLocation getId() {
        return this.key;
    }

    public String getMod() {
        return this.key.getNamespace();
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "title." + this.key.toLanguageKey();
    }

    public String getDescription() {
        return getKey() + ".description";
    }

    public List<Component> tooltip(boolean showTitleModName, String modName) {
        List<Component> list = new ArrayList<>();
        list.add(Component.empty()
                .append(Component.translatable("title.justlevelingfork.requirement_description").withStyle(ChatFormatting.GOLD))
                .append(Component.translatable(getDescription()).withStyle(ChatFormatting.GRAY)));
        if (showTitleModName) {
            list.add(Component.literal(modName).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        }
        return list;
    }

    public List<Component> tooltip() {
        return tooltip(ClientConfigService.showTitleModName(), getMod());
    }
}
