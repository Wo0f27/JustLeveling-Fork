package com.seniors.justlevelingfork.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class FabricRegistryItems {
    private FabricRegistryItems() {
    }

    public static void load() {
        RegistryItems.values().forEach(item ->
                Registry.register(BuiltInRegistries.ITEM, RegistryItems.getId(item), item));
    }
}
