package com.seniors.justlevelingfork.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class FabricRegistrySounds {
    private FabricRegistrySounds() {
    }

    public static void load() {
        RegistrySounds.values().forEach(soundEvent ->
                Registry.register(BuiltInRegistries.SOUND_EVENT, RegistrySounds.getId(soundEvent), soundEvent));
    }
}
