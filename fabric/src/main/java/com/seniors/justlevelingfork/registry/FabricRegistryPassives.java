package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.registry.passive.Passive;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;

public final class FabricRegistryPassives {
    public static final Registry<Passive> REGISTRY =
            FabricRegistryBuilder.createSimple(RegistryPassives.PASSIVES_KEY).buildAndRegister();

    private FabricRegistryPassives() {
    }

    public static void load() {
        RegistryPassives.values().forEach(passive -> Registry.register(REGISTRY, passive.key, passive));
    }
}
