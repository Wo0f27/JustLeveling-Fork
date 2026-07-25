package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.registry.skills.Skill;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;

public final class FabricRegistrySkills {
    public static final Registry<Skill> REGISTRY =
            FabricRegistryBuilder.createSimple(RegistrySkills.SKILLS_KEY).buildAndRegister();

    private FabricRegistrySkills() {
    }

    public static void load() {
        RegistrySkills.values().forEach(skill -> Registry.register(REGISTRY, skill.key, skill));
    }
}
