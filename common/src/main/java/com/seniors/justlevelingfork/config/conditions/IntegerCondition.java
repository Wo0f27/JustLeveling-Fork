package com.seniors.justlevelingfork.config.conditions;

import com.seniors.justlevelingfork.config.models.TitleModel;

abstract class IntegerCondition extends ConditionImpl<Integer> {
    IntegerCondition(String conditionName) {
        super(conditionName);
    }

    @Override
    public boolean meetCondition(String value, TitleModel.EComparator comparator) {
        int parsedValue = Integer.parseInt(value);

        return switch (comparator) {
            case EQUALS -> getProcessedValue().equals(parsedValue);
            case GREATER -> getProcessedValue() > parsedValue;
            case LESS -> getProcessedValue() < parsedValue;
            case GREATER_OR_EQUAL -> getProcessedValue() >= parsedValue;
            case LESS_OR_EQUAL -> getProcessedValue() <= parsedValue;
            default -> false;
        };
    }
}
