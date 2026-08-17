package com.seniors.justlevelingfork.common.feat.effect;

import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import com.seniors.justlevelingfork.common.feat.FeatEffectDefinition;

public final class AbilityScoreImprovementFeatEffect
        implements FeatEffect {

    public static final AbilityScoreImprovementFeatEffect INSTANCE =
            new AbilityScoreImprovementFeatEffect();

    private AbilityScoreImprovementFeatEffect() {
    }

    @Override
    public boolean canApply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {

        if (player == null
                || progress == null
                || feat == null
                || effect == null
                || choice == null
                || choice.isBlank()) {

            return false;
        }

        Aptitude aptitude =
                RegistryAptitudes.getAptitude(
                        choice);

        if (aptitude == null) {
            return false;
        }

        int amount =
                getAmount(
                        effect);

        if (amount <= 0) {
            return false;
        }

        int currentLevel =
                progress.getAptitudeLevel(
                        aptitude);

        return currentLevel + amount
                <= CommonConfigService
                .aptitudeMaxLevel();
    }

    @Override
    public void apply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {

        Aptitude aptitude =
                RegistryAptitudes.getAptitude(
                        choice);

        if (aptitude == null
                || effect == null) {

            return;
        }

        int amount =
                getAmount(
                        effect);

        int currentLevel =
                progress.getAptitudeLevel(
                        aptitude);

        progress.setAptitudeLevel(
                aptitude,
                currentLevel + amount);
    }

    private int getAmount(
            FeatEffectDefinition effectDefinition) {

        JsonObject effect =
                effectDefinition.getData();

        return GsonHelper.getAsInt(
                effect,
                "amount",
                1);
    }
}