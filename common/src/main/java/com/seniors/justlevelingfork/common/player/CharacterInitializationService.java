package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import net.minecraft.resources.ResourceLocation;

public final class CharacterInitializationService {

    public static final int MIN_STARTING_SCORE = 10;
    public static final int MAX_STARTING_SCORE = 15;

    private static final List<Integer> STARTING_SCORE_ARRAY =
            List.of(
                    15,
                    14,
                    13,
                    12,
                    11,
                    10
            );

    private static final List<Aptitude> APTITUDES =
            List.of(
                    RegistryAptitudes.STRENGTH,
                    RegistryAptitudes.DEXTERITY,
                    RegistryAptitudes.CONSTITUTION,
                    RegistryAptitudes.INTELLIGENCE,
                    RegistryAptitudes.WISDOM,
                    RegistryAptitudes.CHARISMA
            );

    private CharacterInitializationService() {
    }

    public static List<Aptitude> aptitudes() {
        return APTITUDES;
    }

    public static List<Integer> startingScoreArray() {
        return STARTING_SCORE_ARRAY;
    }

    public static boolean hasCompletedAbilitySetup(
            ServerPlayer player) {

        return PlayerProgressService.get(player)
                .map(PlayerProgress::isStartingAbilitiesAssigned)
                .orElse(false);
    }

    public static Map<Aptitude, Integer> generateRecommendedAssignment(
            ResourceLocation classId) {

        if (classId == null) {
            return null;
        }

        CharacterClassDefinition definition =
                RegistryClasses.getDefinition(
                        classId);

        if (definition == null
                || definition
                .recommendedAbilityScores()
                .isEmpty()) {
            return null;
        }

        return new LinkedHashMap<>(
                definition.recommendedAbilityScores());
    }

    private static Map<Aptitude, Integer> assignment(
            int strength,
            int dexterity,
            int constitution,
            int intelligence,
            int wisdom,
            int charisma) {

        Map<Aptitude, Integer> assignment =
                new LinkedHashMap<>();

        assignment.put(RegistryAptitudes.STRENGTH, strength);
        assignment.put(RegistryAptitudes.DEXTERITY, dexterity);
        assignment.put(RegistryAptitudes.CONSTITUTION, constitution);
        assignment.put(RegistryAptitudes.INTELLIGENCE, intelligence);
        assignment.put(RegistryAptitudes.WISDOM, wisdom);
        assignment.put(RegistryAptitudes.CHARISMA, charisma);

        return assignment;
    }

    public static ResourceLocation getStartingClass(
            PlayerProgress progress) {

        if (progress == null) {
            return null;
        }

        /*
         * Preferred path: use the explicitly persisted starting
         * class introduced by the class-definition foundation.
         */
        ResourceLocation stored =
                ResourceLocation.tryParse(
                        progress.getStartingClass());

        if (stored != null) {
            return stored;
        }

        /*
         * Legacy/in-memory fallback.
         *
         * Only infer when exactly one class exists.
         */
        if (progress.classLevels.size() != 1) {
            return null;
        }

        Map.Entry<String, Integer> entry =
                progress.classLevels.entrySet()
                        .iterator()
                        .next();

        if (entry.getValue() <= 0) {
            return null;
        }

        return ResourceLocation.tryParse(
                entry.getKey());
    }

    public static Map<Aptitude, Integer>
    generateRecommendedAssignment(PlayerProgress progress) {

        return generateRecommendedAssignment(
                getStartingClass(progress));
    }

    /**
     * Generates a balanced random assignment.
     *
     * Every character receives exactly:
     * 15, 14, 13, 12, 11, 10
     *
     * Only the aptitude receiving each score is randomized.
     */
    public static Map<Aptitude, Integer>
    generateRandomAssignment() {

        List<Integer> shuffledScores =
                new ArrayList<>(STARTING_SCORE_ARRAY);

        Collections.shuffle(shuffledScores);

        Map<Aptitude, Integer> assignment =
                new LinkedHashMap<>();

        for (int i = 0; i < APTITUDES.size(); i++) {
            assignment.put(
                    APTITUDES.get(i),
                    shuffledScores.get(i));
        }

        return assignment;
    }

    /**
     * Commits the player's starting ability scores.
     *
     * This may only happen once and only after the player
     * has exactly one starting class level.
     */
    public static boolean initializeStartingAbilities(
            ServerPlayer player,
            Map<Aptitude, Integer> assignment) {

        if (player == null || !isValidAssignment(assignment)) {
            return false;
        }

        PlayerProgress progress =
                PlayerProgressService.get(player).orElse(null);

        if (progress == null) {
            return false;
        }

        if (progress.isStartingAbilitiesAssigned()) {
            return false;
        }

        // Character creation should already have assigned
        // exactly one starting class.
        if (progress.getCharacterLevel() != 1) {
            return false;
        }

        return PlayerProgressService.update(
                player,
                updated -> {

                    for (Aptitude aptitude : APTITUDES) {

                        int abilityScore =
                                assignment.get(aptitude);

                        int aptitudeLevel =
                                abilityScoreToAptitudeLevel(
                                        abilityScore);

                        updated.setAptitudeLevel(
                                aptitude,
                                aptitudeLevel);
                    }

                    updated.setStartingAbilitiesAssigned(true);
                });
    }

    public static boolean isValidAssignment(
            Map<Aptitude, Integer> assignment) {

        if (assignment == null
                || assignment.size() != APTITUDES.size()
                || !assignment.keySet().containsAll(APTITUDES)) {
            return false;
        }

        List<Integer> scores =
                assignment.values()
                        .stream()
                        .sorted()
                        .toList();

        return scores.equals(
                List.of(
                        10,
                        11,
                        12,
                        13,
                        14,
                        15
                ));
    }

    public static int abilityScoreToAptitudeLevel(
            int abilityScore) {

        if (abilityScore < MIN_STARTING_SCORE
                || abilityScore > MAX_STARTING_SCORE) {
            throw new IllegalArgumentException(
                    "Starting ability score must be between "
                            + MIN_STARTING_SCORE
                            + " and "
                            + MAX_STARTING_SCORE);
        }

        return abilityScore - 9;
    }
}