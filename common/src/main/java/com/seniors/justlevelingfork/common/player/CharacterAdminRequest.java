package com.seniors.justlevelingfork.common.player;

import net.minecraft.resources.ResourceLocation;

public record CharacterAdminRequest(
        CharacterAdminAction action,
        String classId,
        long amount) {

    public CharacterAdminRequest {
        classId =
                classId == null
                        ? ""
                        : classId;
    }

    public static CharacterAdminRequest resetCharacter() {
        return new CharacterAdminRequest(
                CharacterAdminAction.RESET_CHARACTER,
                "",
                0L);
    }

    public static CharacterAdminRequest setStartingClass(
            ResourceLocation classId) {

        return new CharacterAdminRequest(
                CharacterAdminAction.SET_STARTING_CLASS,
                classId == null
                        ? ""
                        : classId.toString(),
                0L);
    }

    public static CharacterAdminRequest
    applyRecommendedAbilities() {

        return new CharacterAdminRequest(
                CharacterAdminAction.APPLY_RECOMMENDED_ABILITIES,
                "",
                0L);
    }

    public static CharacterAdminRequest addXp(
            long amount) {

        return new CharacterAdminRequest(
                CharacterAdminAction.ADD_XP,
                "",
                Math.max(0L, amount));
    }

    public static CharacterAdminRequest xpToNextLevel() {
        return new CharacterAdminRequest(
                CharacterAdminAction.XP_TO_NEXT_LEVEL,
                "",
                0L);
    }

    public static CharacterAdminRequest
    debugEquipmentPenalties() {

        return new CharacterAdminRequest(
                CharacterAdminAction
                        .DEBUG_EQUIPMENT_PENALTIES,
                "",
                0L);
    }
}