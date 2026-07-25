package com.seniors.justlevelingfork.handler;

import com.seniors.justlevelingfork.config.conditions.AdvancementCondition;
import com.seniors.justlevelingfork.config.conditions.AptitudeCondition;
import com.seniors.justlevelingfork.config.conditions.AptitudeLevelProvider;
import com.seniors.justlevelingfork.config.conditions.BlockMinedCondition;
import com.seniors.justlevelingfork.config.conditions.ConditionImpl;
import com.seniors.justlevelingfork.config.conditions.DimensionCondition;
import com.seniors.justlevelingfork.config.conditions.EntityKilledByCondition;
import com.seniors.justlevelingfork.config.conditions.EntityKilledCondition;
import com.seniors.justlevelingfork.config.conditions.ItemBrokenCondition;
import com.seniors.justlevelingfork.config.conditions.ItemCraftedCondition;
import com.seniors.justlevelingfork.config.conditions.ItemDroppedCondition;
import com.seniors.justlevelingfork.config.conditions.ItemPickedUpCondition;
import com.seniors.justlevelingfork.config.conditions.ItemUsedCondition;
import com.seniors.justlevelingfork.config.conditions.StatCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HandlerConditions {
    private static final List<ConditionImpl<?>> CONDITIONS = new ArrayList<>();

    private HandlerConditions() {
    }

    public static void registerDefaults() {
        registerVanillaConditions();
    }

    public static void registerDefaults(AptitudeLevelProvider aptitudeLevelProvider) {
        registerCondition(new AptitudeCondition(aptitudeLevelProvider));
        registerVanillaConditions();
    }

    public static void registerVanillaConditions() {
        registerCondition(new DimensionCondition());
        registerCondition(new EntityKilledCondition());
        registerCondition(new EntityKilledByCondition());
        registerCondition(new StatCondition());
        registerCondition(new BlockMinedCondition());
        registerCondition(new ItemCraftedCondition());
        registerCondition(new ItemUsedCondition());
        registerCondition(new ItemBrokenCondition());
        registerCondition(new ItemPickedUpCondition());
        registerCondition(new ItemDroppedCondition());
        registerCondition(new AdvancementCondition());
    }

    public static void registerCondition(ConditionImpl<?> condition) {
        if (CONDITIONS.stream().anyMatch(c -> c.getConditionName().equalsIgnoreCase(condition.getConditionName()))) {
            throw new IllegalArgumentException(String.format("Condition with name %s already exists!", condition.getConditionName()));
        }

        CONDITIONS.add(condition);
    }

    public static Optional<ConditionImpl<?>> getConditionByName(String conditionName) {
        return CONDITIONS.stream().filter(c -> c.getConditionName().equalsIgnoreCase(conditionName)).findFirst();
    }
}
