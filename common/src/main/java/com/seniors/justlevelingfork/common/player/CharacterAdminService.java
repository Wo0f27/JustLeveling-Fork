package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryClasses;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class CharacterAdminService {

    private CharacterAdminService() {
    }

    public static boolean isAllowed(
            ServerPlayer player) {

        return player != null
                && player.hasPermissions(2);
    }

    public static boolean resetCharacter(
            ServerPlayer player) {

        if (!isAllowed(player)) {
            return false;
        }

        return PlayerProgressService.update(
                player,
                progress -> {

                    progress.resetCharacterProgression();

                    for (Aptitude aptitude
                            : CharacterInitializationService
                            .aptitudes()) {

                        progress.setAptitudeLevel(
                                aptitude,
                                1);
                    }
                });
    }

    /**
     * Creates a clean level-1 test character.
     *
     * This deliberately resets existing character progression
     * so changing the selected class does not accidentally
     * create multiclass test state.
     */
    public static boolean setStartingClass(
            ServerPlayer player,
            ResourceLocation classId) {

        if (!isAllowed(player)
                || classId == null
                || RegistryClasses.getDefinition(classId)
                == null) {

            return false;
        }

        return PlayerProgressService.update(
                player,
                progress -> {

                    progress.resetCharacterProgression();

                    /*
                     * Return raw abilities to score 10.
                     */
                    for (Aptitude aptitude
                            : CharacterInitializationService
                            .aptitudes()) {

                        progress.setAptitudeLevel(
                                aptitude,
                                1);
                    }

                    progress.setStartingClass(
                            classId.toString());

                    progress.setClassLevel(
                            classId.toString(),
                            1);

                    CharacterExperienceService
                            .refreshPendingLevelUps(
                                    progress);
                });
    }

    /**
     * Admin/testing version of recommended abilities.
     *
     * Unlike normal character creation, this may overwrite
     * an existing starting assignment because this interface
     * is explicitly an operator testing tool.
     */
    public static boolean applyRecommendedAbilities(
            ServerPlayer player) {

        if (!isAllowed(player)) {
            return false;
        }

        PlayerProgress current =
                PlayerProgressService.get(player)
                        .orElse(null);

        if (current == null) {
            return false;
        }

        Map<Aptitude, Integer> assignment =
                CharacterInitializationService
                        .generateRecommendedAssignment(
                                current);

        if (assignment == null
                || assignment.isEmpty()) {

            return false;
        }

        return PlayerProgressService.update(
                player,
                progress -> {

                    assignment.forEach(
                            (aptitude, score) ->
                                    progress.setAptitudeLevel(
                                            aptitude,
                                            CharacterInitializationService
                                                    .abilityScoreToAptitudeLevel(
                                                            score)));

                    progress.setStartingAbilitiesAssigned(
                            true);
                });
    }

    public static boolean addXp(
            ServerPlayer player,
            long amount) {

        if (!isAllowed(player)
                || amount <= 0L) {

            return false;
        }

        return CharacterExperienceService
                .addExperience(
                        player,
                        amount);
    }

    public static boolean addXpToNextLevel(
            ServerPlayer player) {

        if (!isAllowed(player)) {
            return false;
        }

        PlayerProgress progress =
                PlayerProgressService.get(player)
                        .orElse(null);

        if (progress == null
                || progress.getCharacterLevel() <= 0
                || progress.getCharacterLevel()
                >= ClassProgressionService
                .MAX_CHARACTER_LEVEL) {

            return false;
        }

        long amount =
                CharacterExperienceService
                        .xpNeededForNextLevel(
                                progress);

        if (amount <= 0L) {
            return false;
        }

        return CharacterExperienceService
                .addExperience(
                        player,
                        amount);
    }
}