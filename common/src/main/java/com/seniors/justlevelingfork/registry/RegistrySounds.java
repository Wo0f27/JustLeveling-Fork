package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class RegistrySounds {
    public static final SoundEvent LIMIT_BREAKER = register("mortal_strike");
    public static final SoundEvent GAIN_TITLE = register("gain_title");

    private static final Map<ResourceLocation, SoundEvent> SOUNDS_BY_ID = new LinkedHashMap<>();

    private RegistrySounds() {
    }

    public static Collection<SoundEvent> values() {
        ensureLoaded();
        return SOUNDS_BY_ID.values();
    }

    public static ResourceLocation getId(SoundEvent soundEvent) {
        return soundEvent.getLocation();
    }

    public static SoundEvent getSound(String name) {
        ensureLoaded();
        return SOUNDS_BY_ID.get(new ResourceLocation(Constants.MOD_ID, name.toLowerCase(Locale.ROOT)));
    }

    private static SoundEvent register(String name) {
        return SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.MOD_ID, name));
    }

    private static void ensureLoaded() {
        if (!SOUNDS_BY_ID.isEmpty()) {
            return;
        }

        put(LIMIT_BREAKER);
        put(GAIN_TITLE);
    }

    private static void put(SoundEvent soundEvent) {
        SOUNDS_BY_ID.put(soundEvent.getLocation(), soundEvent);
    }
}
