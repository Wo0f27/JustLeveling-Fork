package com.seniors.justlevelingfork.registry.skills;

import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.client.core.ValueType;
import com.seniors.justlevelingfork.client.ClientInputState;
import com.seniors.justlevelingfork.common.config.ClientConfigService;
import com.seniors.justlevelingfork.common.config.SkillConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.text.DecimalFormat;
import java.util.Objects;

public class Skill {
    public final ResourceLocation key;
    public final Aptitude aptitude;
    public final int requiredLevel;
    public final ResourceLocation texture;
    private final Value[] configValues;

    public Skill(ResourceLocation skillKey, Aptitude aptitude, int levelRequirement, ResourceLocation skillTexture, Value... skillValues) {
        this.key = skillKey;
        this.aptitude = aptitude;
        this.requiredLevel = levelRequirement;
        this.texture = skillTexture;
        this.configValues = skillValues;
    }

    public Skill get() {
        return this;
    }

    public String getMod() {
        return this.key.getNamespace();
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "skill." + this.key.toLanguageKey();
    }

    public String getDescription() {
        return getKey() + ".description";
    }

    public int getLvl() {
        return SkillConfigService.requiredLevel(getName(), this.requiredLevel);
    }

    public double[] getValue() {
        double[] defaultValue = new double[this.configValues.length];
        for (int i = 0; i < defaultValue.length; i++) {
            defaultValue[i] = 0.0D;
            if (this.configValues[i] != null && this.configValues[i].value instanceof Number value) {
                defaultValue[i] = value.doubleValue();
            }
        }
        return SkillConfigService.values(getName(), defaultValue);
    }

    public MutableComponent getMutableDescription(String description) {
        Object[] newValue = new Object[this.configValues.length];
        double[] values = getValue();
        for (int i = 0; i < newValue.length; i++) {
            if (this.configValues[i] != null) {
                newValue[i] = getParameter(this.configValues[i].type, values[i]);
            }
        }
        return Component.translatable(description, newValue);
    }

    public String getParameter(ValueType type, double parameterValue) {
        DecimalFormat df = new DecimalFormat("0.##");
        String probabilityValue = periodValue(1.0D / parameterValue * 100.0D);
        String parameter = df.format(parameterValue);
        if (type.equals(ValueType.MODIFIER)) {
            parameter = "\u00a7cx" + parameter;
        }
        if (type.equals(ValueType.DURATION)) {
            parameter = "\u00a79" + parameter + "s";
        }
        if (type.equals(ValueType.AMPLIFIER)) {
            parameter = "\u00a76+" + parameter;
        }
        if (type.equals(ValueType.PERCENT)) {
            parameter = "\u00a72" + parameter + "%";
        }
        if (type.equals(ValueType.BOOST)) {
            parameter = "\u00a7d" + intToRoman(Integer.parseInt(parameter));
        }
        if (type.equals(ValueType.PROBABILITY)) {
            parameter = "\u00a7e1/" + parameter + "\u00a7r\u00a77 (\u00a72" + probabilityValue + "%\u00a77\u00a7r)";
        }
        return parameter + "\u00a7r\u00a77";
    }

    public boolean canToggleAtLevel(int aptitudeLevel) {
        int requiredLevel = getLvl();
        return requiredLevel > 0 && aptitudeLevel >= requiredLevel;
    }

    public boolean isEnabled(int aptitudeLevel, boolean toggled) {
        int requiredLevel = getLvl();
        if (requiredLevel < 1) {
            return false;
        }
        return aptitudeLevel >= requiredLevel && toggled;
    }

    public List<Component> tooltip() {
        List<Component> list = new ArrayList<>();
        boolean enabled = PlayerProgressClientState.isSkillEnabled(this);
        boolean canToggle = PlayerProgressClientState.canToggleSkill(this);
        list.add(Component.translatable("tooltip.skill.title")
                .append(Component.translatable(getKey()))
                .withStyle(ChatFormatting.AQUA));
        list.add(Component.translatable("tooltip.skill.description." + (enabled ? "on" : "off"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        list.add(Component.empty());
        if (ClientInputState.hasShiftDown()) {
            list.add(Component.empty()
                    .append(Component.translatable(getKey()).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(getMutableDescription(getDescription()).withStyle(ChatFormatting.GRAY)));
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.skill.description.level_requirement")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            if (canToggle) {
                list.add(Component.literal(" ")
                        .append(Component.translatable(
                                "tooltip.skill.description.available",
                                Component.literal(String.valueOf(getLvl())).withStyle(ChatFormatting.GREEN)))
                        .withStyle(ChatFormatting.DARK_AQUA));
            } else {
                list.add(Component.translatable("tooltip.skill.description.off").withStyle(ChatFormatting.RED));
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

    public ResourceLocation getTexture() {
        return Objects.requireNonNullElse(this.texture, HandlerResources.NULL_SKILL);
    }

    private static String intToRoman(int number) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[number / 1000] + hundreds[number % 1000 / 100] + tens[number % 100 / 10] + units[number % 10];
    }

    private static String periodValue(double value) {
        DecimalFormat df = new DecimalFormat("#.#");
        String number = String.valueOf(value);
        if (number.contains(".")) {
            char[] charArray = number.substring(number.indexOf(".") + 1).toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                int digit = Integer.parseInt(String.valueOf(charArray[i]));
                if (digit > 0 && i > 0) {
                    df = new DecimalFormat("#." + "#".repeat(i + 1));
                    break;
                }
            }
        }
        return df.format(value);
    }
}
