package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class ForgeRegistrySkills {
    private static final DeferredRegister<Skill> REGISTER =
            DeferredRegister.create(RegistrySkills.SKILLS_KEY, Constants.MOD_ID);
    private static final Supplier<IForgeRegistry<Skill>> REGISTRY =
            REGISTER.makeRegistry(() -> new RegistryBuilder<Skill>().disableSaving());

    static {
        RegistrySkills.values().forEach(skill -> REGISTER.register(skill.getName(), () -> skill));
    }

    private ForgeRegistrySkills() {
    }

    public static void load(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static IForgeRegistry<Skill> registry() {
        return REGISTRY.get();
    }
}
