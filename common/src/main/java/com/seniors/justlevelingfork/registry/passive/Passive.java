package com.seniors.justlevelingfork.registry.passive;

import com.seniors.justlevelingfork.client.ClientInputState;
import com.seniors.justlevelingfork.common.config.ClientConfigService;
import com.seniors.justlevelingfork.common.config.PassiveConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class Passive {
    public final ResourceLocation key;
    public final Aptitude aptitude;
    public final ResourceLocation texture;
    public final Attribute attribute;
    public final String attributeUuid;
    public final Object attributeValue;
    public final int[] levelsRequired;

    public Passive(ResourceLocation passiveKey, Aptitude aptitude, ResourceLocation passiveTexture, Attribute attribute, String attributeUuid, Object attributeValue, int... levelsRequired) {
        this.key = passiveKey;
        this.aptitude = aptitude;
        this.texture = passiveTexture;
        this.attribute = attribute;
        this.attributeUuid = attributeUuid;
        this.attributeValue = attributeValue;
        this.levelsRequired = levelsRequired;
    }

    public Passive get() {
        return this;
    }

    public String getMod() {
        return this.key.getNamespace();
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "passive." + this.key.toLanguageKey();
    }

    public String getDescription() {
        return getKey() + ".description";
    }

    public double getValue() {
        double defaultValue = 0.0D;
        if (this.attributeValue instanceof Number value) {
            defaultValue = value.doubleValue();
        }
        return PassiveConfigService.value(getName(), defaultValue);
    }

    public int getNextLevelUp(int currentLevel) {
        int[] levels = getLevelsRequired();
        int[] requirement = new int[levels.length + 2];
        requirement[0] = 0;
        System.arraycopy(levels, 0, requirement, 1, levels.length);
        return requirement[currentLevel + 1];
    }

    public List<Component> tooltip() {
        int level = PlayerProgressClientState.passiveLevel(this);
        int maxLevel = getMaxLevel();
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        String valuePerLevel = decimalFormat.format(getValue() / maxLevel);
        String valueActualLevel = decimalFormat.format(getValue() / maxLevel * level);
        String valueMaxLevel = decimalFormat.format(getValue());

        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("tooltip.passive.title")
                .append(Component.translatable(getKey()))
                .withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("tooltip.passive.description.passive_level", level, maxLevel)
                .withStyle(ChatFormatting.GRAY));
        list.add(Component.empty());
        if (ClientInputState.hasShiftDown()) {
            list.add(Component.empty()
                    .append(Component.translatable(getKey()).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable(getDescription()).withStyle(ChatFormatting.GRAY)));
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.passive.description.other_info").withStyle(ChatFormatting.GRAY));
            list.add(Component.literal(" ")
                    .append(Component.translatable("tooltip.passive.description.level", valuePerLevel))
                    .withStyle(ChatFormatting.DARK_GREEN));
            list.add(Component.literal(" ")
                    .append(Component.translatable("tooltip.passive.description.actual_level", valueActualLevel))
                    .withStyle(ChatFormatting.DARK_GREEN));
            list.add(Component.literal(" ")
                    .append(Component.translatable("tooltip.passive.description.max_level", valueMaxLevel))
                    .withStyle(ChatFormatting.DARK_GREEN));
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.passive.description.level_requirement")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            if (level < maxLevel) {
                list.add(Component.literal(" ")
                        .append(Component.translatable(
                                "tooltip.passive.description.passive_required",
                                Component.translatable(this.aptitude.getKey()).withStyle(ChatFormatting.GREEN),
                                Component.literal(String.valueOf(getNextLevelUp(level))).withStyle(ChatFormatting.GREEN),
                                Component.literal(String.valueOf(level + 1)).withStyle(ChatFormatting.GREEN)))
                        .withStyle(ChatFormatting.DARK_AQUA));
            } else {
                list.add(Component.literal(" ")
                        .append(Component.translatable("tooltip.passive.description.passive_max_level"))
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
        } else {
            list.add(Component.translatable("tooltip.general.description.more_information")
                    .withStyle(ChatFormatting.YELLOW));
        }
        if (ClientConfigService.showSkillModName()) {
            list.add(Component.literal(getMod()).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        }
        return list;
    }

    public int getMaxLevel() {
        return getLevelsRequired().length;
    }

    public int[] getLevelsRequired() {
        return PassiveConfigService.levels(getName(), this.levelsRequired);
    }

    public ResourceLocation getTexture() {
        return Objects.requireNonNullElse(this.texture, HandlerResources.NULL_SKILL);
    }
}
