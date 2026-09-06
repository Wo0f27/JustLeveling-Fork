package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.registry.ForgeRegistryMobEffects;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Server-authoritative sprint-distance tracking for the Charger feat.
 *
 * Checkpoint 1 only builds and displays Momentum. Charge collision, stun,
 * knockback and Momentum consumption are intentionally added later so the
 * movement tracker can be validated in isolation first.
 */
public final class ChargerFeatIntegration {

    public static final ResourceLocation CHARGER =
            new ResourceLocation(Constants.MOD_ID, "charger");

    private static final double MOMENTUM_I_DISTANCE = 4.0D;
    private static final double MOMENTUM_II_DISTANCE = 8.0D;
    private static final double MOMENTUM_III_DISTANCE = 12.0D;

    /**
     * Real sprint movement is far below this value. A larger one-tick jump is
     * treated as a teleport/non-physical displacement and clears Momentum
     * instead of allowing it to charge the feat.
     */
    private static final double MAX_HORIZONTAL_DELTA_PER_TICK = 4.0D;

    /**
     * After sprinting stops, Momentum holds for two seconds before dropping
     * one tier. Further tiers decay at the same interval: III -> II -> I -> 0.
     */
    private static final int MOMENTUM_DECAY_INTERVAL_TICKS = 40;

    /**
     * The effect is only an indicator and is refreshed before this expires.
     */
    private static final int MOMENTUM_EFFECT_DURATION_TICKS = 20;
    private static final int MOMENTUM_EFFECT_REFRESH_THRESHOLD_TICKS = 8;

    private static final Map<ServerPlayer, ChargeState> STATES =
            new WeakHashMap<>();

    private ChargerFeatIntegration() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(ChargerFeatIntegration.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (!FeatProgressionService.hasFeat(player, CHARGER)) {
            clear(player);
            return;
        }

        ChargeState state = STATES.computeIfAbsent(
                player,
                ignored -> new ChargeState(player.getX(), player.getZ()));

        double currentX = player.getX();
        double currentZ = player.getZ();
        double deltaX = currentX - state.lastX;
        double deltaZ = currentZ - state.lastZ;
        double horizontalDelta = Math.sqrt(
                deltaX * deltaX + deltaZ * deltaZ);

        // Always advance the sample position so idle time cannot later be
        // counted as sprint distance when sprinting resumes.
        state.lastX = currentX;
        state.lastZ = currentZ;

        if (isInvalidChargeMovement(player)) {
            resetMomentum(player, state);
            return;
        }

        if (!player.isSprinting()) {
            state.decayTicks++;

            if (state.decayTicks >= MOMENTUM_DECAY_INTERVAL_TICKS) {
                decayMomentum(state);
                state.decayTicks = 0;
            }

            syncMomentumEffect(player, getMomentumLevel(state.distance));
            return;
        }

        state.decayTicks = 0;

        if (horizontalDelta > MAX_HORIZONTAL_DELTA_PER_TICK) {
            resetMomentum(player, state);
            return;
        }

        if (horizontalDelta > 0.0D) {
            state.distance = Math.min(
                    MOMENTUM_III_DISTANCE,
                    state.distance + horizontalDelta);
        }

        syncMomentumEffect(player, getMomentumLevel(state.distance));
    }

    private static boolean isInvalidChargeMovement(ServerPlayer player) {
        return !player.isAlive()
                || player.isDeadOrDying()
                || player.isRemoved()
                || player.isPassenger()
                || player.isFallFlying()
                || player.isSwimming()
                || player.getAbilities().flying
                || player.isSpectator();
    }

    private static int getMomentumLevel(double distance) {
        if (distance >= MOMENTUM_III_DISTANCE) {
            return 3;
        }

        if (distance >= MOMENTUM_II_DISTANCE) {
            return 2;
        }

        if (distance >= MOMENTUM_I_DISTANCE) {
            return 1;
        }

        return 0;
    }

    private static void decayMomentum(ChargeState state) {
        int momentumLevel = getMomentumLevel(state.distance);

        if (momentumLevel >= 3) {
            state.distance = MOMENTUM_II_DISTANCE;
        } else if (momentumLevel == 2) {
            state.distance = MOMENTUM_I_DISTANCE;
        } else {
            state.distance = 0.0D;
        }
    }

    private static void syncMomentumEffect(
            ServerPlayer player,
            int momentumLevel) {

        if (momentumLevel <= 0) {
            player.removeEffect(ForgeRegistryMobEffects.MOMENTUM.get());
            return;
        }

        int amplifier = momentumLevel - 1;
        MobEffectInstance current =
                player.getEffect(ForgeRegistryMobEffects.MOMENTUM.get());

        if (current != null
                && current.getAmplifier() == amplifier
                && current.getDuration()
                > MOMENTUM_EFFECT_REFRESH_THRESHOLD_TICKS) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                ForgeRegistryMobEffects.MOMENTUM.get(),
                MOMENTUM_EFFECT_DURATION_TICKS,
                amplifier,
                false,
                false,
                true));
    }

    private static void resetMomentum(
            ServerPlayer player,
            ChargeState state) {

        state.distance = 0.0D;
        state.decayTicks = 0;
        player.removeEffect(ForgeRegistryMobEffects.MOMENTUM.get());
    }

    private static void clear(ServerPlayer player) {
        STATES.remove(player);

        if (player != null) {
            player.removeEffect(ForgeRegistryMobEffects.MOMENTUM.get());
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(
            PlayerEvent.PlayerChangedDimensionEvent event) {

        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            STATES.remove(player);
        }
    }

    private static final class ChargeState {
        private double lastX;
        private double lastZ;
        private double distance;
        private int decayTicks;

        private ChargeState(double lastX, double lastZ) {
            this.lastX = lastX;
            this.lastZ = lastZ;
        }
    }
}
