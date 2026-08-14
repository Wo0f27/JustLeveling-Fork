package com.seniors.justlevelingfork.common.player;

import net.minecraft.server.level.ServerPlayer;

public final class CharacterExperienceService {

    /*
     * Cumulative XP required to reach each character level.
     *
     * Levels 1-20 follow D&D 5e 2014.
     * Levels 21-30 are custom epic/mythic progression.
     *
     * Index = character level.
     */
    private static final long[] LEVEL_THRESHOLDS = {
            0L,          // unused index 0

            0L,          // 1
            300L,        // 2
            900L,        // 3
            2_700L,      // 4
            6_500L,      // 5
            14_000L,     // 6
            23_000L,     // 7
            34_000L,     // 8
            48_000L,     // 9
            64_000L,     // 10
            85_000L,     // 11
            100_000L,    // 12
            120_000L,    // 13
            140_000L,    // 14
            165_000L,    // 15
            195_000L,    // 16
            225_000L,    // 17
            265_000L,    // 18
            305_000L,    // 19
            355_000L,    // 20

            410_000L,    // 21
            470_000L,    // 22
            535_000L,    // 23
            605_000L,    // 24
            680_000L,    // 25
            760_000L,    // 26
            845_000L,    // 27
            935_000L,    // 28
            1_030_000L,  // 29
            1_130_000L   // 30
    };

    private CharacterExperienceService() {
    }

    public static long getCharacterXp(ServerPlayer player) {
        return PlayerProgressService.get(player)
                .map(PlayerProgress::getCharacterXp)
                .orElse(0L);
    }

    public static long xpForLevel(int level) {
        int safeLevel = Math.max(
                1,
                Math.min(
                        ClassProgressionService.MAX_CHARACTER_LEVEL,
                        level));

        return LEVEL_THRESHOLDS[safeLevel];
    }

    public static int levelForXp(long xp) {
        long safeXp = Math.max(0L, xp);

        for (int level =
             ClassProgressionService.MAX_CHARACTER_LEVEL;
             level >= 1;
             level--) {

            if (safeXp >= LEVEL_THRESHOLDS[level]) {
                return level;
            }
        }

        return 1;
    }

    public static long xpForNextLevel(
            PlayerProgress progress) {

        if (progress == null) {
            return xpForLevel(2);
        }

        int level = progress.getCharacterLevel();

        if (level >= ClassProgressionService.MAX_CHARACTER_LEVEL) {
            return xpForLevel(
                    ClassProgressionService.MAX_CHARACTER_LEVEL);
        }

        return xpForLevel(level + 1);
    }

    public static long xpNeededForNextLevel(
            PlayerProgress progress) {

        if (progress == null) {
            return xpForLevel(2);
        }

        if (progress.getCharacterLevel()
                >= ClassProgressionService.MAX_CHARACTER_LEVEL) {
            return 0L;
        }

        return Math.max(
                0L,
                xpForNextLevel(progress)
                        - progress.getCharacterXp());
    }

    public static boolean addExperience(
            ServerPlayer player,
            long amount) {

        if (player == null || amount <= 0L) {
            return false;
        }

        return PlayerProgressService.update(
                player,
                progress -> {

                    long current =
                            progress.getCharacterXp();

                    long maximum =
                            xpForLevel(
                                    ClassProgressionService
                                            .MAX_CHARACTER_LEVEL);

                    long added;

                    if (amount > maximum - current) {
                        added = maximum;
                    } else {
                        added = current + amount;
                    }

                    progress.setCharacterXp(added);

                    refreshPendingLevelUps(progress);
                });
    }

    public static boolean setExperience(
            ServerPlayer player,
            long amount) {

        if (player == null) {
            return false;
        }

        long safeAmount = Math.max(
                0L,
                Math.min(
                        amount,
                        xpForLevel(
                                ClassProgressionService
                                        .MAX_CHARACTER_LEVEL)));

        return PlayerProgressService.update(
                player,
                progress -> {

                    progress.setCharacterXp(safeAmount);

                    refreshPendingLevelUps(progress);
                });
    }

    public static void refreshPendingLevelUps(
            PlayerProgress progress) {

        if (progress == null) {
            return;
        }

        int currentLevel =
                progress.getCharacterLevel();

        // No class yet means character creation is incomplete.
        if (currentLevel <= 0) {
            progress.setPendingLevelUps(0);
            return;
        }

        int xpLevel =
                levelForXp(progress.getCharacterXp());

        int availableLevels =
                Math.max(0, xpLevel - currentLevel);

        progress.setPendingLevelUps(availableLevels);
    }
}