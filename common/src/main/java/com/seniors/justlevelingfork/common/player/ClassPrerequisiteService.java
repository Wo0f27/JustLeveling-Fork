package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryClasses;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ClassPrerequisiteService {

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

        /*
         * D&D multiclassing requires meeting the prerequisite
         * of every class the character already has.
         */
        for (Map.Entry<String, Integer> entry
                : progress.classLevels.entrySet()) {

            if (entry.getValue() <= 0) {
                continue;
            }

            ResourceLocation existingClass =
                    ResourceLocation.tryParse(
                            entry.getKey());

            if (existingClass != null
                    && !meetsClassRequirement(
                    player,
                    progress,
                    existingClass)) {
                return false;
            }
        }

        /*
         * The character must also meet the prerequisite
         * of the class being entered.
         */
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

        CharacterClassDefinition definition =
                RegistryClasses.getDefinition(
                        classId);

        /*
         * Preserve the previous behavior for unknown/addon
         * classes until a public class-registration API exists.
         */
        if (definition == null) {
            return true;
        }

        return definition
                .multiclassRequirement()
                .meets(
                        aptitude ->
                                score(
                                        player,
                                        progress,
                                        aptitude));
    }

    private static int score(
            ServerPlayer player,
            PlayerProgress progress,
            Aptitude aptitude) {

        /*
         * IMPORTANT:
         * This intentionally uses the effective score.
         *
         * External ancestry bonuses therefore continue
         * counting toward multiclass requirements.
         */
        return AbilityScoreBonusService.getAbilityScore(
                player,
                progress,
                aptitude);
    }
}