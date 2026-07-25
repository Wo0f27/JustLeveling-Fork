package com.seniors.justlevelingfork.config.conditions;

import com.seniors.justlevelingfork.config.models.TitleModel;
import net.minecraft.server.level.ServerPlayer;

public abstract class ConditionImpl<T> {
    private final String conditionName;
    private T processedValue;

    public ConditionImpl(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getConditionName() {
        return conditionName;
    }

    public T getProcessedValue() {
        return processedValue;
    }

    public void setProcessedValue(T value) {
        processedValue = value;
    }

    public abstract void processVariable(String value, ServerPlayer serverPlayer);

    public abstract boolean meetCondition(String value, TitleModel.EComparator comparator);
}
