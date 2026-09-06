package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.effect.FeatEffectRegistry;
import com.seniors.justlevelingfork.common.feat.effect.GrantWeaponProficiencyFeatEffect;
import com.seniors.justlevelingfork.common.feat.effect.GrantWeaponProficiencyFeatEffect.ParsedConfig;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.common.proficiency.WeaponProficiencyService;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FeatWeaponProficiencyService {

    private static final ResourceLocation PROVIDER_ID =
            new ResourceLocation(
                    Constants.MOD_ID,
                    "feat_weapon_proficiencies");

    private FeatWeaponProficiencyService() {
    }

    public static void register() {

        WeaponProficiencyService.registerProvider(
                PROVIDER_ID,
                FeatWeaponProficiencyService::getProficiencies);
    }

    private static Set<ResourceLocation> getProficiencies(
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

        LinkedHashSet<ResourceLocation> result =
                new LinkedHashSet<>();

        for (FeatDefinition feat
                : FeatManager.INSTANCE.values()) {

            if (feat == null
                    || feat.getId() == null) {

                continue;
            }

            String featId =
                    feat.getId().toString();

            if (progress.getFeatRank(featId) <= 0) {
                continue;
            }

            String storedChoice =
                    progress.getFeatChoice(featId);

            for (FeatEffectDefinition effect
                    : feat.getEffects()) {

                if (!FeatEffectRegistry
                        .GRANT_WEAPON_PROFICIENCY
                        .equals(effect.getType())) {

                    continue;
                }

                ParsedConfig config =
                        GrantWeaponProficiencyFeatEffect
                                .parseConfig(effect);

                if (config == null) {
                    continue;
                }

                result.addAll(
                        config.fixedProficiencies());

                if (config.choicesRequired() > 0) {

                    result.addAll(
                            GrantWeaponProficiencyFeatEffect
                                    .parseSelectedProficiencies(
                                            config,
                                            storedChoice));
                }
            }
        }

        if (result.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                new LinkedHashSet<>(result));
    }
}
