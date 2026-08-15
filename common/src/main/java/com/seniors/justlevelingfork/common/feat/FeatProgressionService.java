package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.common.feat.effect.FeatEffect;
import com.seniors.justlevelingfork.common.feat.effect.FeatEffectRegistry;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FeatProgressionService {

    private FeatProgressionService() {
    }

    public static int getFeatRank(
            ServerPlayer player,
            ResourceLocation featId) {

        if (player == null || featId == null) {
            return 0;
        }

        return PlayerProgressService.get(player)
                .map(progress ->
                        progress.getFeatRank(
                                featId.toString()))
                .orElse(0);
    }

    public static boolean hasFeat(
            ServerPlayer player,
            ResourceLocation featId) {

        return getFeatRank(
                player,
                featId) > 0;
    }

    public static boolean canTakeFeat(
            ServerPlayer player,
            ResourceLocation featId,
            String choice) {

        if (player == null || featId == null) {
            return false;
        }

        FeatDefinition feat =
                FeatManager.INSTANCE.get(featId);

        if (feat == null) {
            return false;
        }

        PlayerProgress progress =
                PlayerProgressService.get(player)
                        .orElse(null);

        if (progress == null) {
            return false;
        }

        if (progress.getPendingAdvancements() <= 0) {
            return false;
        }

        if (progress.getCharacterLevel()
                < feat.getMinimumCharacterLevel()) {
            return false;
        }

        int currentRank =
                progress.getFeatRank(
                        featId.toString());

        if (!feat.canGainRank(currentRank)) {
            return false;
        }

        FeatEffect effect =
                FeatEffectRegistry.get(
                        feat.getEffectType());

        if (effect == null) {
            return false;
        }

        return effect.canApply(
                player,
                progress,
                feat,
                choice == null ? "" : choice);
    }

    public static boolean takeFeat(
            ServerPlayer player,
            ResourceLocation featId,
            String choice) {

        if (!canTakeFeat(
                player,
                featId,
                choice)) {

            return false;
        }

        FeatDefinition feat =
                FeatManager.INSTANCE.get(featId);

        FeatEffect effect =
                FeatEffectRegistry.get(
                        feat.getEffectType());

        PlayerProgress progress =
                PlayerProgressService.get(player)
                        .orElse(null);

        if (progress == null || effect == null) {
            return false;
        }

        int currentRank =
                progress.getFeatRank(
                        featId.toString());

        String safeChoice =
                choice == null
                        ? ""
                        : choice;

        return PlayerProgressService.update(
                player,
                updated -> {

                    /*
                     * Apply the actual datapack-defined effect.
                     */
                    effect.apply(
                            player,
                            updated,
                            feat,
                            safeChoice);

                    /*
                     * Record ownership/rank.
                     */
                    updated.setFeatRank(
                            featId.toString(),
                            currentRank + 1);

                    /*
                     * One feat consumes one advancement.
                     */
                    updated.setPendingAdvancements(
                            Math.max(
                                    0,
                                    updated.getPendingAdvancements() - 1));
                });
    }
}