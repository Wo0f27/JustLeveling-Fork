package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryClasses;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.seniors.justlevelingfork.common.proficiency.EquipmentPenaltyProfile;
import com.seniors.justlevelingfork.common.proficiency.EquipmentPenaltyService;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.seniors.justlevelingfork.common.player.ExternalAttributeService;

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

    public static boolean sendEquipmentPenaltyDebug(
            ServerPlayer player) {

        if (!isAllowed(player)) {
            return false;
        }

        EquipmentPenaltyProfile profile =
                EquipmentPenaltyService
                        .resolve(player);

        player.sendSystemMessage(
                Component.literal(
                        "=== JLF Equipment Penalty Debug ==="));

        player.sendSystemMessage(
                Component.literal(
                        "Main-Hand: "
                                + (profile
                                .mainHandNonProficient()
                                ? "NOT PROFICIENT ["
                                + profile
                                .mainHandWeaponType()
                                .name()
                                + "]"
                                : "NO PENALTY")));

        player.sendSystemMessage(
                Component.literal(
                        "Off-Hand: "
                                + (profile
                                .offHandNonProficient()
                                ? "NOT PROFICIENT ["
                                + profile
                                .offHandWeaponType()
                                .name()
                                + "]"
                                : "NO PENALTY")));

        player.sendSystemMessage(
                Component.literal(
                        "Highest Non-Proficient Armor: "
                                + (profile
                                .highestNonProficientArmor()
                                == null
                                ? "NONE"
                                : profile
                                .highestNonProficientArmor()
                                .name())));

        player.sendSystemMessage(
                Component.literal(
                        "Non-Proficient Shield: "
                                + (profile
                                .nonProficientShield()
                                ? "YES"
                                : "NO")));

        player.sendSystemMessage(
                Component.literal(
                        "--- Resolved Penalties ---"));

        player.sendSystemMessage(
                Component.literal(
                        "Attack Damage: -"
                                + percent(
                                profile
                                        .attackDamageReduction())));

        player.sendSystemMessage(
                Component.literal(
                        "Attack Speed: -"
                                + percent(
                                profile
                                        .attackSpeedReduction())));

        player.sendSystemMessage(
                Component.literal(
                        "Movement Speed: -"
                                + percent(
                                profile
                                        .movementSpeedReduction())));

        player.sendSystemMessage(
                Component.literal(
                        "Projectile Damage: -"
                                + percent(
                                profile
                                        .projectileDamageReduction())));

        player.sendSystemMessage(
                Component.literal(
                        "Draw Speed: -"
                                + percent(
                                profile
                                        .drawSpeedReduction())));

        player.sendSystemMessage(
                Component.literal(
                        "Cast Time: +"
                                + percent(
                                profile
                                        .castTimeIncrease())));

        player.sendSystemMessage(
                Component.literal(
                        "Mana Cost: +"
                                + percent(
                                profile
                                        .manaCostIncrease())));

        player.sendSystemMessage(
                Component.literal(
                        "--- Live Attributes ---"));

        player.sendSystemMessage(
                Component.literal(
                        "Attack Damage: "
                                + attributeValue(
                                player,
                                Attributes.ATTACK_DAMAGE,
                                2)));

        player.sendSystemMessage(
                Component.literal(
                        "Attack Speed: "
                                + attributeValue(
                                player,
                                Attributes.ATTACK_SPEED,
                                2)));

        player.sendSystemMessage(
                Component.literal(
                        "Movement Speed: "
                                + attributeValue(
                                player,
                                Attributes.MOVEMENT_SPEED,
                                4)));

        player.sendSystemMessage(
                Component.literal(
                        "Arrow Damage: "
                                + String.format(
                                Locale.ROOT,
                                "%.3f",
                                ExternalAttributeService
                                        .getValue(
                                                player,
                                                "attributeslib",
                                                "arrow_damage",
                                                0.0D))));

        player.sendSystemMessage(
                Component.literal(
                        "Draw Speed: "
                                + String.format(
                                Locale.ROOT,
                                "%.3f",
                                ExternalAttributeService
                                        .getValue(
                                                player,
                                                "attributeslib",
                                                "draw_speed",
                                                0.0D))));

        return true;
    }

    private static String percent(
            double value) {

        return String.format(
                Locale.ROOT,
                "%.0f%%",
                value * 100.0D);
    }

    private static String attributeValue(
            ServerPlayer player,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            int decimals) {

        double value =
                player.getAttributeValue(
                        attribute);

        return String.format(
                Locale.ROOT,
                "%."
                        + Math.max(
                        0,
                        decimals)
                        + "f",
                value);
    }
}