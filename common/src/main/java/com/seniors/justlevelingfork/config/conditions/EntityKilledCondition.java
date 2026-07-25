package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;

public class EntityKilledCondition extends EntityStatCondition {
    public EntityKilledCondition() {
        super("EntityKilled", Stats.ENTITY_KILLED);
    }
}
