package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;

public class StatCondition extends RegistryStatCondition<net.minecraft.resources.ResourceLocation> {
    public StatCondition() {
        super("Stat", Stats.CUSTOM, "Stat");
    }
}
