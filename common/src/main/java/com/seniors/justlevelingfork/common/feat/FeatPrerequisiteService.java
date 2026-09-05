package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.common.player.AbilityScoreBonusService;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.proficiency.ArmorProficiencyService;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FeatPrerequisiteService {

    private FeatPrerequisiteService() {
    }

    public static boolean meetsPrerequisites(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat) {

        if (player == null
                || progress == null
                || feat == null) {

            return false;
        }

        FeatPrerequisites requirements =
                feat.getPrerequisites();

        if (progress.getCharacterLevel()
                < requirements
                .minimumCharacterLevel()) {

            return false;
        }

        if (!meetsAbilityRequirements(
                player,
                progress,
                requirements)) {

            return false;
        }

        if (!meetsClassRequirements(
                progress,
                requirements)) {

            return false;
        }

        if (!meetsFeatRequirements(
                progress,
                requirements)) {

            return false;
        }

        return meetsArmorProficiencyRequirements(
                player,
                requirements);
    }

    private static boolean meetsAbilityRequirements(
            ServerPlayer player,
            PlayerProgress progress,
            FeatPrerequisites requirements) {

        for (Map.Entry<String, Integer> entry
                : requirements
                .abilities()
                .entrySet()) {

            Aptitude aptitude =
                    RegistryAptitudes
                            .getAptitude(
                                    entry.getKey());

            if (aptitude == null) {
                return false;
            }

            int effectiveScore =
                    AbilityScoreBonusService
                            .getAbilityScore(
                                    player,
                                    progress,
                                    aptitude);

            if (effectiveScore
                    < entry.getValue()) {

                return false;
            }
        }

        return true;
    }

    private static boolean meetsClassRequirements(
            PlayerProgress progress,
            FeatPrerequisites requirements) {

        for (Map.Entry<ResourceLocation, Integer> entry
                : requirements
                .classes()
                .entrySet()) {

            int actualLevel =
                    progress.getClassLevel(
                            entry.getKey()
                                    .toString());

            if (actualLevel
                    < entry.getValue()) {

                return false;
            }
        }

        return true;
    }

    private static boolean meetsFeatRequirements(
            PlayerProgress progress,
            FeatPrerequisites requirements) {

        for (Map.Entry<ResourceLocation, Integer> entry
                : requirements
                .feats()
                .entrySet()) {

            int actualRank =
                    progress.getFeatRank(
                            entry.getKey()
                                    .toString());

            if (actualRank
                    < entry.getValue()) {

                return false;
            }
        }

        return true;
    }

    private static boolean
    meetsArmorProficiencyRequirements(
            ServerPlayer player,
            FeatPrerequisites requirements) {

        if (requirements
                .armorProficiencies()
                .isEmpty()) {

            return true;
        }

        return ArmorProficiencyService
                .getProficiencies(player)
                .containsAll(
                        requirements
                                .armorProficiencies());
    }
}