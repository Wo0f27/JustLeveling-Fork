package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class ClassAbilityRequirement {

    private final int minimumScore;
    private final List<List<Aptitude>> alternatives;

    private ClassAbilityRequirement(
            int minimumScore,
            List<List<Aptitude>> alternatives) {

        this.minimumScore = Math.max(0, minimumScore);

        this.alternatives = alternatives == null
                ? List.of()
                : alternatives.stream()
                .map(List::copyOf)
                .toList();
    }

    /**
     * Requires every supplied ability.
     *
     * Example:
     * Monk = DEX 13 AND WIS 13.
     */
    public static ClassAbilityRequirement allOf(
            int minimumScore,
            Aptitude... aptitudes) {

        if (aptitudes == null || aptitudes.length == 0) {
            return none();
        }

        return new ClassAbilityRequirement(
                minimumScore,
                List.of(List.of(aptitudes)));
    }

    /**
     * Requires any one of the supplied abilities.
     *
     * Example:
     * Fighter = STR 13 OR DEX 13.
     */
    public static ClassAbilityRequirement anyOf(
            int minimumScore,
            Aptitude... aptitudes) {

        if (aptitudes == null || aptitudes.length == 0) {
            return none();
        }

        List<List<Aptitude>> alternatives =
                new ArrayList<>();

        for (Aptitude aptitude : aptitudes) {
            if (aptitude != null) {
                alternatives.add(List.of(aptitude));
            }
        }

        return new ClassAbilityRequirement(
                minimumScore,
                alternatives);
    }

    public static ClassAbilityRequirement none() {
        return new ClassAbilityRequirement(
                0,
                List.of());
    }

    public boolean meets(
            ToIntFunction<Aptitude> scoreLookup) {

        if (alternatives.isEmpty()) {
            return true;
        }

        if (scoreLookup == null) {
            return false;
        }

        /*
         * At least one alternative must succeed.
         *
         * Every aptitude contained inside that alternative
         * must meet the minimum.
         *
         * Fighter:
         * [STR] OR [DEX]
         *
         * Monk:
         * [DEX, WIS]
         */
        for (List<Aptitude> alternative : alternatives) {

            boolean meetsAlternative = true;

            for (Aptitude aptitude : alternative) {
                if (aptitude == null
                        || scoreLookup.applyAsInt(aptitude)
                        < minimumScore) {

                    meetsAlternative = false;
                    break;
                }
            }

            if (meetsAlternative) {
                return true;
            }
        }

        return false;
    }

    public int minimumScore() {
        return minimumScore;
    }

    public List<List<Aptitude>> alternatives() {
        return alternatives;
    }
}