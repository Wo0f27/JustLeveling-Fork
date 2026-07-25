package com.seniors.justlevelingfork.common.integration;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.server.level.ServerPlayer;

public final class FTBQuestProgress {
    private FTBQuestProgress() {
    }

    public static boolean hasAptitudeLevel(ServerPlayer player, String aptitudeName, int level) {
        Aptitude aptitude = getAptitude(aptitudeName);
        return aptitude != null && PlayerProgressService.get(player)
                .map(progress -> progress.getAptitudeLevel(aptitude) >= clampAptitudeLevel(level))
                .orElse(false);
    }

    public static boolean setAptitudeLevel(ServerPlayer player, String aptitudeName, int level, boolean onlyIncrease) {
        Aptitude aptitude = getAptitude(aptitudeName);
        if (aptitude == null) {
            return false;
        }

        int requestedLevel = clampAptitudeLevel(level);
        if (onlyIncrease) {
            int clampedLevel = requestedLevel;
            requestedLevel = PlayerProgressService.get(player)
                    .map(progress -> Math.max(progress.getAptitudeLevel(aptitude), clampedLevel))
                    .orElse(clampedLevel);
        }
        return PlayerProgressService.setAptitudeLevel(player, aptitude, requestedLevel, CommonConfigService.aptitudeMaxLevel());
    }

    public static boolean unlockTitle(ServerPlayer player, String titleName, boolean equipTitle) {
        Title title = getTitle(titleName);
        if (title == null || !PlayerProgressService.setUnlockTitle(player, title, true)) {
            return false;
        }

        return !equipTitle || PlayerProgressService.setPlayerTitle(player, title);
    }

    private static Aptitude getAptitude(String aptitudeName) {
        return RegistryAptitudes.getAptitude(aptitudeName);
    }

    private static Title getTitle(String titleName) {
        return RegistryTitles.getTitle(titleName);
    }

    private static int clampAptitudeLevel(int level) {
        return Math.max(1, Math.min(level, CommonConfigService.aptitudeMaxLevel()));
    }
}
