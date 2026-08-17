package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AbilityScoreClientState {

    private static Map<String, Integer> scores =
            Map.of();

    private AbilityScoreClientState() {
    }

    public static void replace(
            Map<String, Integer> newScores) {

        Map<String, Integer> updated =
                new LinkedHashMap<>();

        if (newScores != null) {

            newScores.forEach((name, score) -> {

                if (name == null
                        || name.isBlank()
                        || score == null) {
                    return;
                }

                updated.put(
                        name.toLowerCase(Locale.ROOT),
                        score);
            });
        }

        scores =
                Map.copyOf(updated);
    }

    public static Integer get(
            String abilityName) {

        if (abilityName == null
                || abilityName.isBlank()) {
            return null;
        }

        return scores.get(
                abilityName.toLowerCase(
                        Locale.ROOT));
    }

    public static Integer get(
            Aptitude aptitude) {

        if (aptitude == null) {
            return null;
        }

        return get(
                aptitude.getName());
    }

    public static boolean has(
            String abilityName) {

        return get(abilityName) != null;
    }

    public static Map<String, Integer> values() {
        return scores;
    }

    public static void clear() {
        scores = Map.of();
    }
}