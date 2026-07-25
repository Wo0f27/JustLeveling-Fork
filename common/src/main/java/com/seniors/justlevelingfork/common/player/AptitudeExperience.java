package com.seniors.justlevelingfork.common.player;

import net.minecraft.world.entity.player.Player;

public final class AptitudeExperience {
    private AptitudeExperience() {
    }

    public static int requiredPoints(int aptitudeLevel, int firstCostLevel) {
        return getExperienceForLevel(requiredExperienceLevels(aptitudeLevel, firstCostLevel));
    }

    public static int requiredExperienceLevels(int aptitudeLevel, int firstCostLevel) {
        return clampToInt((long) aptitudeLevel + firstCostLevel - 1L);
    }

    public static int getPlayerXP(Player player) {
        long wholeLevels = getExperienceForLevel(player.experienceLevel);
        long progress = (long) (player.experienceProgress * player.getXpNeededForNextLevel());
        return clampToInt(wholeLevels + progress);
    }

    public static void addPlayerXP(Player player, int amount) {
        int experience = clampToInt(Math.max(0L, (long) getPlayerXP(player) + amount));
        player.totalExperience = experience;
        player.experienceLevel = getLevelForExperience(experience);
        int expForLevel = getExperienceForLevel(player.experienceLevel);
        player.experienceProgress = (experience - expForLevel) / (float) player.getXpNeededForNextLevel();
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) {
                return level;
            }
            level++;
            targetXp -= xpToNextLevel;
        }
    }

    public static int getExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 15) {
            return sum(level, 7, 2);
        }
        if (level <= 30) {
            return clampToInt(315L + sum(level - 15, 37, 5));
        }
        return clampToInt(1395L + sum(level - 30, 112, 9));
    }

    public static int xpBarCap(int level) {
        if (level >= 30) {
            return clampToInt(112L + (long) (level - 30) * 9L);
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    private static int sum(int n, int a0, int d) {
        return clampToInt((long) n * (2L * a0 + (long) (n - 1) * d) / 2L);
    }

    private static int clampToInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, value));
    }
}
