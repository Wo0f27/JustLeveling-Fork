package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class RegistryClasses {

    public static final ResourceLocation BARBARIAN = id("barbarian");
    public static final ResourceLocation BARD = id("bard");
    public static final ResourceLocation CLERIC = id("cleric");
    public static final ResourceLocation DRUID = id("druid");
    public static final ResourceLocation FIGHTER = id("fighter");
    public static final ResourceLocation MONK = id("monk");
    public static final ResourceLocation PALADIN = id("paladin");
    public static final ResourceLocation RANGER = id("ranger");
    public static final ResourceLocation ROGUE = id("rogue");
    public static final ResourceLocation SORCERER = id("sorcerer");
    public static final ResourceLocation WARLOCK = id("warlock");
    public static final ResourceLocation WIZARD = id("wizard");

    private static final List<ResourceLocation> VALUES = List.of(
            BARBARIAN,
            BARD,
            CLERIC,
            DRUID,
            FIGHTER,
            MONK,
            PALADIN,
            RANGER,
            ROGUE,
            SORCERER,
            WARLOCK,
            WIZARD
    );

    private RegistryClasses() {
    }

    public static List<ResourceLocation> values() {
        return VALUES;
    }

    public static ResourceLocation get(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        ResourceLocation id = ResourceLocation.tryParse(
                value.contains(":")
                        ? value
                        : Constants.MOD_ID + ":" + value);

        return id != null && VALUES.contains(id) ? id : null;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }
}