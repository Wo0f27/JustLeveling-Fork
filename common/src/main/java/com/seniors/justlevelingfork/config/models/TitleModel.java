package com.seniors.justlevelingfork.config.models;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.config.conditions.ConditionImpl;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * Drops malformed entries and copies mutable fields so title reloads cannot
     * publish null data into the registry or requirement evaluator.
     */
    public static List<TitleModel> sanitizedList(Collection<TitleModel> models) {
        List<TitleModel> sanitized = new ArrayList<>();
        if (models == null) {
            return sanitized;
        }

        for (TitleModel model : models) {
            if (model == null || !isValidTitleId(model.TitleId)) {
                continue;
            }
            List<String> conditions = new ArrayList<>();
            if (model.Conditions != null) {
                for (String condition : model.Conditions) {
                    if (condition != null && !condition.isBlank()) {
                        conditions.add(condition);
                    }
                }
            }
            TitleModel copy = new TitleModel(
                    model.TitleId,
                    conditions,
                    model.Default,
                    Boolean.TRUE.equals(model.HideRequirements));
            sanitized.add(copy);
        }
        return sanitized;
    }

    private static boolean isValidTitleId(String titleId) {
        if (titleId == null || titleId.isBlank() || titleId.indexOf(':') >= 0) {
            return false;
        }
        try {
            new ResourceLocation(Constants.MOD_ID, titleId);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", TitleId, String.join("=", Conditions == null ? List.of() : Conditions), Default);
    }

    public boolean checkRequirements(ServerPlayer serverPlayer, ConditionResolver conditionResolver) {
        if (Default) {
            return true;
        }

        List<String> conditions = Conditions == null ? List.of() : Conditions;
        int passedConditions = 0;
        for (String condition : conditions) {
            if (condition == null || condition.isBlank()) {
                Constants.LOG.error(">> Error! Title {} has an empty condition.", TitleId);
                continue;
            }
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

        return passedConditions == conditions.size();
    }

    public Title createTitle() {
        ResourceLocation id;
        try {
            id = new ResourceLocation(Constants.MOD_ID, TitleId);
        } catch (IllegalArgumentException | NullPointerException exception) {
            Constants.LOG.error(">> Invalid title id '{}', using rookie instead.", TitleId);
            id = new ResourceLocation(Constants.MOD_ID, "rookie");
        }
        title = new Title(id, Default, Boolean.TRUE.equals(this.HideRequirements));
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
