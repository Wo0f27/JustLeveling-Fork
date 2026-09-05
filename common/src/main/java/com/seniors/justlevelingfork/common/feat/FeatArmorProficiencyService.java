package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.effect.FeatEffectRegistry;
import com.seniors.justlevelingfork.common.feat.effect.GrantArmorProficiencyFeatEffect;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import com.seniors.justlevelingfork.common.proficiency.ArmorProficiencyService;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FeatArmorProficiencyService {

    private static final ResourceLocation PROVIDER_ID =
            new ResourceLocation(
                    Constants.MOD_ID,
                    "feat_armor_proficiencies");

    private FeatArmorProficiencyService() {
    }

    public static void register() {

        ArmorProficiencyService.registerProvider(
                PROVIDER_ID,
                FeatArmorProficiencyService::getProficiencies);
    }

    private static Set<ArmorCategory> getProficiencies(
            ServerPlayer player) {

        if (player == null) {
            return Set.of();
        }

        PlayerProgress progress =
                PlayerProgressService.get(player)
                        .orElse(null);

        if (progress == null) {
            return Set.of();
        }

        EnumSet<ArmorCategory> result =
                EnumSet.noneOf(
                        ArmorCategory.class);

        /*
         * Feat definitions remain datapack-authoritative.
         *
         * PlayerProgress stores only feat ownership/rank.
         * The actual granted categories are derived from
         * the currently loaded feat definitions.
         */
        for (FeatDefinition feat
                : FeatManager.INSTANCE.values()) {

            if (feat == null
                    || feat.getId() == null) {

                continue;
            }

            int rank =
                    progress.getFeatRank(
                            feat.getId()
                                    .toString());

            if (rank <= 0) {
                continue;
            }

            for (FeatEffectDefinition effect
                    : feat.getEffects()) {

                if (!FeatEffectRegistry
                        .GRANT_ARMOR_PROFICIENCY
                        .equals(
                                effect.getType())) {

                    continue;
                }

                result.addAll(
                        GrantArmorProficiencyFeatEffect
                                .parseCategories(
                                        effect));
            }
        }

        if (result.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                EnumSet.copyOf(result));
    }
}