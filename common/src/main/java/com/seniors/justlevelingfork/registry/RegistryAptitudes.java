package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistryAptitudes {
    public static final ResourceKey<Registry<Aptitude>> APTITUDES_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, "aptitudes"));

    public static final Aptitude STRENGTH =
            register(0, "strength", HandlerResources.STRENGTH_LOCKED_ICON, "yellow_terracotta");

    public static final Aptitude DEXTERITY =
            register(1, "dexterity", HandlerResources.DEXTERITY_LOCKED_ICON, "blue_terracotta");

    public static final Aptitude CONSTITUTION =
            register(2, "constitution", HandlerResources.CONSTITUTION_LOCKED_ICON, "red_terracotta");

    public static final Aptitude INTELLIGENCE =
            register(3, "intelligence", HandlerResources.INTELLIGENCE_LOCKED_ICON, "orange_terracotta");

    public static final Aptitude WISDOM =
            register(4, "wisdom", HandlerResources.WISDOM_LOCKED_ICON, "cyan_terracotta");

    public static final Aptitude CHARISMA =
            register(5, "charisma", HandlerResources.CHARISMA_LOCKED_ICON, "purple_terracotta");


    private static final Map<String, Aptitude> APTITUDES_BY_NAME = new LinkedHashMap<>();

    private RegistryAptitudes() {
    }

    public static Collection<Aptitude> values() {
        ensureLoaded();
        return APTITUDES_BY_NAME.values();
    }

    public static Aptitude getAptitude(String aptitudeName) {
        if (aptitudeName == null || aptitudeName.isBlank()) {
            return null;
        }

        ensureLoaded();
        return APTITUDES_BY_NAME.get(path(aptitudeName));
    }

    private static Aptitude register(int index, String name, ResourceLocation[] lockedTexture, String backgroundBlock) {
        ResourceLocation key = new ResourceLocation(Constants.MOD_ID, name);
        ResourceLocation background = new ResourceLocation("minecraft", "textures/block/" + backgroundBlock + ".png");
        return new Aptitude(index, key, lockedTexture, background);
    }

    private static void ensureLoaded() {
        if (!APTITUDES_BY_NAME.isEmpty()) {
            return;
        }

        put(STRENGTH);
        put(DEXTERITY);
        put(CONSTITUTION);
        put(INTELLIGENCE);
        put(WISDOM);
        put(CHARISMA);
    }

    private static void put(Aptitude aptitude) {
        APTITUDES_BY_NAME.put(aptitude.getName(), aptitude);
    }

    private static String path(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0) {
            return normalized.substring(namespaceSeparator + 1);
        }
        return normalized;
    }
}
