package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatCooldownService;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.network.ForgeServerNetworking;
import com.seniors.justlevelingfork.registry.ForgeRegistryMobEffects;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Server-authoritative hostile-proximity detection for the Alert feat.
 *
 * Each hostile entity UUID can trigger the warning only once for a given
 * player while that player instance is active. Leaving and re-entering the
 * radius does not retrigger the same mob.
 */
public final class AlertFeatIntegration {

    public static final ResourceLocation ALERT =
            new ResourceLocation(Constants.MOD_ID, "alert");

    private static final double DETECTION_RADIUS = 6.0D;
    private static final double DETECTION_RADIUS_SQR =
            DETECTION_RADIUS * DETECTION_RADIUS;
    private static final int SCAN_INTERVAL_TICKS = 5;

    private static final int REACTION_DURATION_TICKS = 4 * 20;
    private static final int REACTION_COOLDOWN_TICKS = 60 * 20;

    private static final Map<ServerPlayer, Set<UUID>> SEEN_HOSTILES =
            new WeakHashMap<>();

    private AlertFeatIntegration() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(AlertFeatIntegration.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        if (!FeatProgressionService.hasFeat(player, ALERT)) {
            SEEN_HOSTILES.remove(player);
            FeatCooldownService.clear(player, ALERT);
            return;
        }

        Set<UUID> seen = SEEN_HOSTILES.computeIfAbsent(
                player,
                ignored -> new HashSet<>());

        int newlyDetected = 0;

        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(DETECTION_RADIUS),
                entity -> isHostileToPlayer(player, entity))) {

            if (entity.distanceToSqr(player) > DETECTION_RADIUS_SQR) {
                continue;
            }

            if (seen.add(entity.getUUID())) {
                newlyDetected++;
            }
        }

        if (newlyDetected <= 0) {
            return;
        }

        // Threat awareness itself never has a cooldown.
        ForgeServerNetworking.sendAlertWarning(
                player,
                newlyDetected);

        tryTriggerReaction(player);
    }

    private static void tryTriggerReaction(ServerPlayer player) {
        if (!FeatCooldownService.tryStart(
                player,
                ALERT,
                REACTION_COOLDOWN_TICKS)) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                ForgeRegistryMobEffects.ALERT_REACTION.get(),
                REACTION_DURATION_TICKS,
                0,
                false,
                false,
                true));
    }

    private static boolean isHostileToPlayer(
            ServerPlayer player,
            LivingEntity entity) {

        if (entity == null
                || entity == player
                || !entity.isAlive()
                || entity.isRemoved()) {
            return false;
        }

        if (entity instanceof Enemy) {
            return true;
        }

        if (entity.getType().getCategory() == MobCategory.MONSTER) {
            return true;
        }

        return entity instanceof Mob mob
                && mob.getTarget() == player;
    }
}
