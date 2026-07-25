package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;

public final class FabricRegistryAptitudes {
    public static final Registry<Aptitude> REGISTRY =
            FabricRegistryBuilder.createSimple(RegistryAptitudes.APTITUDES_KEY).buildAndRegister();

    private FabricRegistryAptitudes() {
    }

    public static void load() {
        RegistryAptitudes.values().forEach(aptitude -> Registry.register(REGISTRY, aptitude.key, aptitude));
    }
}
