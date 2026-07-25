package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class ForgeRegistryAptitudes {
    private static final DeferredRegister<Aptitude> REGISTER =
            DeferredRegister.create(RegistryAptitudes.APTITUDES_KEY, Constants.MOD_ID);
    private static final Supplier<IForgeRegistry<Aptitude>> REGISTRY =
            REGISTER.makeRegistry(() -> new RegistryBuilder<Aptitude>().disableSaving());

    static {
        RegistryAptitudes.values().forEach(aptitude -> REGISTER.register(aptitude.getName(), () -> aptitude));
    }

    private ForgeRegistryAptitudes() {
    }

    public static void load(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static IForgeRegistry<Aptitude> registry() {
        return REGISTRY.get();
    }
}
