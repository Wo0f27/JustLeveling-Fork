package com.seniors.justlevelingfork.config.models;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.config.conditions.ConditionImpl;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TitleModel {
    public String TitleId;
    public List<String> Conditions = new ArrayList<>();
    public boolean Default;
    public Boolean HideRequirements = false;

    private transient Title title;

    public TitleModel() {
        TitleId = "rookie";
        Conditions = new ArrayList<>();
        Default = true;
        HideRequirements = false;
    }

    public TitleModel(String titleID, List<String> conditions, boolean isDefault) {
        TitleId = titleID;
        Conditions = conditions;
        Default = isDefault;
        HideRequirements = false;
    }

    public TitleModel(String titleID, List<String> conditions, boolean isDefault, boolean hideRequirements) {
        TitleId = titleID;
        Conditions = conditions;
        Default = isDefault;
        HideRequirements = hideRequirements;
    }

    public Title getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", TitleId, String.join("=", Conditions), Default);
    }

    public boolean checkRequirements(ServerPlayer serverPlayer, ConditionResolver conditionResolver) {
        if (Default) {
            return true;
        }

        byte passedConditions = 0;
        for (String condition : Conditions) {
            String[] split = condition.split("/");

            if (split.length != 4) {
                Constants.LOG.error(">> Error! Title {} have a wrong formatted condition. (General)", TitleId);
                continue;
            }

            EComparator comparator;
            try {
                comparator = EComparator.valueOf(split[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                Constants.LOG.error(">> Error! Title {} have a wrong formatted condition. (Comparator)", TitleId);
                continue;
            }

            Optional<ConditionImpl<?>> conditionImpl = conditionResolver.getConditionByName(split[0]);
            if (conditionImpl.isEmpty()) {
                Constants.LOG.error(">> Error! Title {} have a wrong formatted condition. (Condition type or Comparator)", TitleId);
                continue;
            }

            conditionImpl.get().processVariable(split[1], serverPlayer);
            if (conditionImpl.get().meetCondition(split[3], comparator)) {
                passedConditions++;
            }
        }

        return passedConditions == Conditions.size();
    }

    public Title createTitle() {
        title = new Title(new ResourceLocation(Constants.MOD_ID, TitleId), Default, this.HideRequirements);
        return title;
    }

    public enum EConditionType {
        Aptitude,
        Stat,
        EntityKilled,
        Special
    }

    public enum EComparator {
        EQUALS,
        GREATER,
        LESS,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL
    }

    @FunctionalInterface
    public interface ConditionResolver {
        Optional<ConditionImpl<?>> getConditionByName(String name);
    }
}
