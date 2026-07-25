package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class ForgeRegistryTitles {
    private static final DeferredRegister<Title> REGISTER =
            DeferredRegister.create(RegistryTitles.TITLES_KEY, Constants.MOD_ID);
    private static final Supplier<IForgeRegistry<Title>> REGISTRY =
            REGISTER.makeRegistry(() -> new RegistryBuilder<Title>().disableSaving());

    static {
        RegistryTitles.defaults().forEach(title -> REGISTER.register(title.getName(), () -> title));
    }

    private ForgeRegistryTitles() {
    }

    public static void load(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static IForgeRegistry<Title> registry() {
        return REGISTRY.get();
    }
}
