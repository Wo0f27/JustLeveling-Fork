package com.seniors.justlevelingfork.common.player;

public final class AbilityScoreService {

    private AbilityScoreService() {
    }

    /**
     * JLF aptitude level 1 represents a base DnD ability score of 10.
     *
     * Level 1 -> 10
     * Level 2 -> 11
     * Level 3 -> 12
     * Level 4 -> 13
     * etc.
     */
    public static int abilityScore(int aptitudeLevel) {
        return abilityScore(aptitudeLevel, 0);
    }

    /**
     * Same calculation, but allows an external bonus such as an ancestry bonus.
     *
     * Example:
     * Constitution aptitude level 1 + Dwarf bonus 2
     * = ability score 12.
     */
    public static int abilityScore(int aptitudeLevel, int externalBonus) {
        int safeLevel = Math.max(1, aptitudeLevel);
        return 9 + safeLevel + externalBonus;
    }

    public static int abilityModifier(int aptitudeLevel) {
        return abilityModifier(aptitudeLevel, 0);
    }

    public static int abilityModifier(int aptitudeLevel, int externalBonus) {
        int score = abilityScore(aptitudeLevel, externalBonus);
        return Math.floorDiv(score - 10, 2);
    }
}