package com.seniors.justlevelingfork.common.feat;

import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Persistent, server-authoritative cooldown storage for feat mechanics.
 *
 * Cooldowns are keyed by feat id and stored as the server game tick at which
 * the feat becomes ready again. Keeping this generic lets future feats reuse
 * the same persistence instead of introducing one-off runtime maps.
 */
public final class FeatCooldownService {

    private FeatCooldownService() {
    }

    public static boolean isReady(
            ServerPlayer player,
            ResourceLocation featId) {

        return remainingTicks(player, featId) <= 0L;
    }

    public static long remainingTicks(
            ServerPlayer player,
            ResourceLocation featId) {

        if (player == null || featId == null) {
            return 0L;
        }

        PlayerProgress progress = PlayerProgressService.get(player).orElse(null);
        if (progress == null) {
            return 0L;
        }

        long readyAt = progress.getFeatCooldownReadyAt(featId.toString());
        long remaining = readyAt - currentGameTick(player);

        if (remaining <= 0L && readyAt > 0L) {
            progress.setFeatCooldownReadyAt(featId.toString(), 0L);
            return 0L;
        }

        return Math.max(0L, remaining);
    }

    public static boolean tryStart(
            ServerPlayer player,
            ResourceLocation featId,
            long durationTicks) {

        if (player == null
                || featId == null
                || durationTicks <= 0L) {
            return false;
        }

        PlayerProgress progress = PlayerProgressService.get(player).orElse(null);
        if (progress == null) {
            return false;
        }

        long now = currentGameTick(player);
        long readyAt = progress.getFeatCooldownReadyAt(featId.toString());

        if (readyAt > now) {
            return false;
        }

        progress.setFeatCooldownReadyAt(
                featId.toString(),
                now + durationTicks);
        return true;
    }

    public static void clear(
            ServerPlayer player,
            ResourceLocation featId) {

        if (player == null || featId == null) {
            return;
        }

        PlayerProgressService.get(player).ifPresent(progress ->
                progress.setFeatCooldownReadyAt(featId.toString(), 0L));
    }

    private static long currentGameTick(ServerPlayer player) {
        return player.serverLevel()
                .getServer()
                .overworld()
                .getGameTime();
    }
}
