package com.seniors.justlevelingfork.common.feat.effect;

import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

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
            String choice) {

        if (player == null
                || progress == null
                || feat == null
                || choice == null
                || choice.isBlank()) {

            return false;
        }

        Aptitude aptitude =
                RegistryAptitudes.getAptitude(choice);

        if (aptitude == null) {
            return false;
        }

        int amount =
                getAmount(feat);

        if (amount <= 0) {
            return false;
        }

        int currentLevel =
                progress.getAptitudeLevel(aptitude);

        /*
         * Do not partially apply ASI.
         *
         * Example:
         * current 29
         * JSON amount 2
         *
         * must fail rather than silently becoming 30.
         */
        return currentLevel + amount
                <= CommonConfigService.aptitudeMaxLevel();
    }

    @Override
    public void apply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            String choice) {

        Aptitude aptitude =
                RegistryAptitudes.getAptitude(choice);

        if (aptitude == null) {
            return;
        }

        int amount =
                getAmount(feat);

        int currentLevel =
                progress.getAptitudeLevel(aptitude);

        progress.setAptitudeLevel(
                aptitude,
                currentLevel + amount);
    }

    private int getAmount(
            FeatDefinition feat) {

        JsonObject effect =
                feat.getEffectData();

        return GsonHelper.getAsInt(
                effect,
                "amount",
                1);
    }
}