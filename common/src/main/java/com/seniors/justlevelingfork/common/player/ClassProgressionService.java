package com.seniors.justlevelingfork.common.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ClassProgressionService {

    public static final int MAX_CHARACTER_LEVEL = 30;

    private ClassProgressionService() {
    }

    public static int getCharacterLevel(ServerPlayer player) {
        return PlayerProgressService.get(player)
                .map(PlayerProgress::getCharacterLevel)
                .orElse(0);
    }

    public static int getClassLevel(
            ServerPlayer player,
            ResourceLocation classId) {

        if (classId == null) {
            return 0;
        }

        return PlayerProgressService.get(player)
                .map(progress -> progress.getClassLevel(classId.toString()))
                .orElse(0);
    }

    public static boolean hasClass(
            ServerPlayer player,
            ResourceLocation classId) {

        return getClassLevel(player, classId) > 0;
    }

    /**
     * Intended for character creation integrations such as
     * Origins: Ancestries.
     *
     * Only succeeds when the character has no class yet.
     */
    public static boolean setInitialClass(
            ServerPlayer player,
            ResourceLocation classId) {

        if (player == null || classId == null) {
            return false;
        }

        PlayerProgress progress =
                PlayerProgressService.get(player).orElse(null);

        if (progress == null || progress.getCharacterLevel() != 0) {
            return false;
        }

        return PlayerProgressService.update(
                player,
                updated -> updated.setClassLevel(
                        classId.toString(),
                        1));
    }

    /**
     * Adds one level to an existing class or begins a new class.
     *
     * Multiclass prerequisites will be added separately.
     */
    public static boolean addClassLevel(
            ServerPlayer player,
            ResourceLocation classId) {

        if (player == null || classId == null) {
            return false;
        }

        PlayerProgress progress =
                PlayerProgressService.get(player).orElse(null);

        if (progress == null
                || progress.getCharacterLevel() >= MAX_CHARACTER_LEVEL) {
            return false;
        }

        int currentClassLevel =
                progress.getClassLevel(classId.toString());

        if (currentClassLevel == 0
                && progress.getCharacterLevel() > 0
                && !ClassPrerequisiteService.canMulticlassInto(
                player,
                progress,
                classId)) {
            return false;
        }

        return PlayerProgressService.update(
                player,
                updated -> updated.setClassLevel(
                        classId.toString(),
                        currentClassLevel + 1));
    }

    public static boolean setClassLevel(
            ServerPlayer player,
            ResourceLocation classId,
            int level) {

        if (player == null || classId == null || level < 0) {
            return false;
        }

        PlayerProgress progress =
                PlayerProgressService.get(player).orElse(null);

        if (progress == null) {
            return false;
        }

        int currentClassLevel =
                progress.getClassLevel(classId.toString());

        boolean enteringNewClass =
                currentClassLevel == 0
                        && level > 0
                        && progress.getCharacterLevel() > 0;

        if (enteringNewClass
                && !ClassPrerequisiteService.canMulticlassInto(
                player,
                progress,
                classId)) {
            return false;
        }

        int otherClassLevels =
                progress.getCharacterLevel() - currentClassLevel;

        if (otherClassLevels + level > MAX_CHARACTER_LEVEL) {
            return false;
        }

        return PlayerProgressService.update(
                player,
                updated -> updated.setClassLevel(
                        classId.toString(),
                        level));
    }

    public static String getSubclass(
            ServerPlayer player,
            ResourceLocation classId) {

        if (classId == null) {
            return "";
        }

        return PlayerProgressService.get(player)
                .map(progress ->
                        progress.getSubclass(classId.toString()))
                .orElse("");
    }

    public static boolean setSubclass(
            ServerPlayer player,
            ResourceLocation classId,
            ResourceLocation subclassId) {

        if (player == null
                || classId == null
                || subclassId == null
                || !hasClass(player, classId)) {
            return false;
        }

        return PlayerProgressService.update(
                player,
                progress -> progress.setSubclass(
                        classId.toString(),
                        subclassId.toString()));
    }
}