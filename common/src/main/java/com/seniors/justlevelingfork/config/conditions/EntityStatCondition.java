package com.seniors.justlevelingfork.config.conditions;

import com.seniors.justlevelingfork.Constants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.EntityType;

abstract class EntityStatCondition extends IntegerCondition {
    private final StatType<EntityType<?>> statType;

    EntityStatCondition(String conditionName, StatType<EntityType<?>> statType) {
        super(conditionName);
        this.statType = statType;
    }

    @Override
    public void processVariable(String value, ServerPlayer serverPlayer) {
        EntityType.byString(value).ifPresentOrElse(
                entityType -> setProcessedValue(serverPlayer.getStats().getValue(statType.get(entityType))),
                () -> {
                    Constants.LOG.error(">> Error! Entity name {} not found!", value);
                    setProcessedValue(0);
                });
    }
}
