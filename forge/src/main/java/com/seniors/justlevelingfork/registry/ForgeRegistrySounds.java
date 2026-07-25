package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeRegistrySounds {
    private static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Constants.MOD_ID);

    static {
        RegistrySounds.values().forEach(soundEvent ->
                REGISTER.register(RegistrySounds.getId(soundEvent).getPath(), () -> soundEvent));
    }

    private ForgeRegistrySounds() {
    }

    public static void load(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
