package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ClassPrerequisiteService {

    private static final int MULTICLASS_SCORE_REQUIREMENT = 13;

    private ClassPrerequisiteService() {
    }

    public static boolean canMulticlassInto(
            ServerPlayer player,
            PlayerProgress progress,
            ResourceLocation targetClass) {

        if (player == null
                || progress == null
                || targetClass == null) {
            return false;
        }

        // D&D multiclassing requires meeting the prerequisite
        // of every class the character already has.
        for (Map.Entry<String, Integer> entry
                : progress.classLevels.entrySet()) {

            if (entry.getValue() <= 0) {
                continue;
            }

            ResourceLocation existingClass =
                    ResourceLocation.tryParse(entry.getKey());

            if (existingClass != null
                    && !meetsClassRequirement(
                    player,
                    progress,
                    existingClass)) {
                return false;
            }
        }

        // And the prerequisite of the class being entered.
        return meetsClassRequirement(
                player,
                progress,
                targetClass);
    }

    public static boolean meetsClassRequirement(
            ServerPlayer player,
            PlayerProgress progress,
            ResourceLocation classId) {

        if (classId == null) {
            return false;
        }

        if (classId.equals(RegistryClasses.BARBARIAN)) {
            return score(player, progress, RegistryAptitudes.STRENGTH) >= 13;
        }

        if (classId.equals(RegistryClasses.BARD)) {
            return score(player, progress, RegistryAptitudes.CHARISMA) >= 13;
        }

        if (classId.equals(RegistryClasses.CLERIC)) {
            return score(player, progress, RegistryAptitudes.WISDOM) >= 13;
        }

        if (classId.equals(RegistryClasses.DRUID)) {
            return score(player, progress, RegistryAptitudes.WISDOM) >= 13;
        }

        if (classId.equals(RegistryClasses.FIGHTER)) {
            return score(player, progress, RegistryAptitudes.STRENGTH) >= 13
                    || score(player, progress, RegistryAptitudes.DEXTERITY) >= 13;
        }

        if (classId.equals(RegistryClasses.MONK)) {
            return score(player, progress, RegistryAptitudes.DEXTERITY) >= 13
                    && score(player, progress, RegistryAptitudes.WISDOM) >= 13;
        }

        if (classId.equals(RegistryClasses.PALADIN)) {
            return score(player, progress, RegistryAptitudes.STRENGTH) >= 13
                    && score(player, progress, RegistryAptitudes.CHARISMA) >= 13;
        }

        if (classId.equals(RegistryClasses.RANGER)) {
            return score(player, progress, RegistryAptitudes.DEXTERITY) >= 13
                    && score(player, progress, RegistryAptitudes.WISDOM) >= 13;
        }

        if (classId.equals(RegistryClasses.ROGUE)) {
            return score(player, progress, RegistryAptitudes.DEXTERITY) >= 13;
        }

        if (classId.equals(RegistryClasses.SORCERER)) {
            return score(player, progress, RegistryAptitudes.CHARISMA) >= 13;
        }

        if (classId.equals(RegistryClasses.WARLOCK)) {
            return score(player, progress, RegistryAptitudes.CHARISMA) >= 13;
        }

        if (classId.equals(RegistryClasses.WIZARD)) {
            return score(player, progress, RegistryAptitudes.INTELLIGENCE) >= 13;
        }

        // Unknown/addon classes currently have no prerequisite.
        return true;
    }

    private static int score(
            ServerPlayer player,
            PlayerProgress progress,
            com.seniors.justlevelingfork.registry.aptitude.Aptitude aptitude) {

        return AbilityScoreBonusService.getAbilityScore(
                player,
                progress,
                aptitude);
    }
}