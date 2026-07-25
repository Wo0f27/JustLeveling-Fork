package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.registry.title.Title;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;

public final class FabricRegistryTitles {
    public static final Registry<Title> REGISTRY =
            FabricRegistryBuilder.createSimple(RegistryTitles.TITLES_KEY).buildAndRegister();

    private FabricRegistryTitles() {
    }

    public static void load() {
        RegistryTitles.defaults().forEach(title -> Registry.register(REGISTRY, title.getId(), title));
    }
}
