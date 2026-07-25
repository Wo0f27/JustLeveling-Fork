package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.registry.passive.Passive;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class ForgeRegistryPassives {
    private static final DeferredRegister<Passive> REGISTER =
            DeferredRegister.create(RegistryPassives.PASSIVES_KEY, Constants.MOD_ID);
    private static final Supplier<IForgeRegistry<Passive>> REGISTRY =
            REGISTER.makeRegistry(() -> new RegistryBuilder<Passive>().disableSaving());

    static {
        RegistryPassives.values().forEach(passive -> REGISTER.register(passive.getName(), () -> passive));
    }

    private ForgeRegistryPassives() {
    }

    public static void load(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static IForgeRegistry<Passive> registry() {
        return REGISTRY.get();
    }
}
