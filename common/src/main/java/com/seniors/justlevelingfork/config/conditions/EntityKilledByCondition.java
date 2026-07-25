package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;

public class EntityKilledByCondition extends EntityStatCondition {
    public EntityKilledByCondition() {
        super("EntiyKilledBy", Stats.ENTITY_KILLED_BY);
    }
}
