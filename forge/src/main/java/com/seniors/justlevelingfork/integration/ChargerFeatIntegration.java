package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.registry.ForgeRegistryMobEffects;
import com.w0of26.martialspells.combat.StunService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Server-authoritative Momentum and body-impact handling for the Charger feat.
 */
public final class ChargerFeatIntegration {

    public static final ResourceLocation CHARGER =
            new ResourceLocation(Constants.MOD_ID, "charger");

    private static final double MOMENTUM_I_DISTANCE = 4.0D;
    private static final double MOMENTUM_II_DISTANCE = 8.0D;
    private static final double MOMENTUM_III_DISTANCE = 12.0D;

    /**
     * Real sprint movement is far below these values. Larger one-tick jumps
     * are treated as teleport/non-physical displacement and clear Momentum.
     */
    private static final double MAX_HORIZONTAL_DELTA_PER_TICK = 4.0D;
    private static final double MAX_VERTICAL_DELTA_PER_TICK = 4.0D;

    /**
     * After sprinting stops, Momentum holds for two seconds before dropping
     * one tier. Further tiers decay at the same interval: III -> II -> I -> 0.
     */
    private static final int MOMENTUM_DECAY_INTERVAL_TICKS = 40;

    /**
     * Charger impact values by Momentum tier.
     *
     * Stun durations intentionally stay short because Charger is a passive
     * feat that can be rebuilt by movement rather than a resource-spending
     * technique like Stunning Strike.
     */
    private static final int[] IMPACT_STUN_DURATION_TICKS = {
            10, // Momentum I:   0.5 seconds
            20, // Momentum II:  1.0 second
            30  // Momentum III: 1.5 seconds
    };

    private static final double[] IMPACT_KNOCKBACK_STRENGTH = {
            0.60D,
            1.00D,
            1.40D
    };

    /**
     * The effect is only an indicator and is refreshed before this expires.
     */
    private static final int MOMENTUM_EFFECT_DURATION_TICKS = 20;
    private static final int MOMENTUM_EFFECT_REFRESH_THRESHOLD_TICKS = 8;

    private static final double COLLISION_SEARCH_INFLATION = 0.35D;
    private static final double COLLISION_EPSILON = 0.10D;

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
                ignored -> new ChargeState(
                        player.getX(),
                        player.getY(),
                        player.getZ()));

        double previousX = state.lastX;
        double previousY = state.lastY;
        double previousZ = state.lastZ;
        double currentX = player.getX();
        double currentY = player.getY();
        double currentZ = player.getZ();

        double deltaX = currentX - previousX;
        double deltaY = currentY - previousY;
        double deltaZ = currentZ - previousZ;
        double horizontalDelta = Math.sqrt(
                deltaX * deltaX + deltaZ * deltaZ);

        // Always advance the sample position so idle/decay time cannot later
        // be counted as sprint distance when sprinting resumes.
        state.lastX = currentX;
        state.lastY = currentY;
        state.lastZ = currentZ;

        if (isInvalidChargeMovement(player)) {
            resetMomentum(player, state);
            return;
        }

        if (horizontalDelta > MAX_HORIZONTAL_DELTA_PER_TICK
                || Math.abs(deltaY) > MAX_VERTICAL_DELTA_PER_TICK) {
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

        if (horizontalDelta > 0.0D) {
            state.distance = Math.min(
                    MOMENTUM_III_DISTANCE,
                    state.distance + horizontalDelta);
        }

        int momentumLevel = getMomentumLevel(state.distance);
        syncMomentumEffect(player, momentumLevel);

        if (momentumLevel <= 0 || horizontalDelta <= 1.0E-4D) {
            return;
        }

        LivingEntity target = findChargeTarget(
                player,
                previousX,
                previousY,
                previousZ,
                currentX,
                currentY,
                currentZ);

        if (target == null) {
            return;
        }

        performChargeImpact(
                player,
                target,
                momentumLevel,
                deltaX,
                deltaZ);

        // A successful body collision always consumes the entire charge,
        // even when the target is stun-immune or highly knockback-resistant.
        resetMomentum(player, state);
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

    /**
     * Finds the first valid creature intersected by the player's swept body
     * volume between the previous and current server-authoritative positions.
     * This avoids relying on a single end-of-tick AABB overlap, which can miss
     * targets at higher modded movement speeds.
     */
    private static LivingEntity findChargeTarget(
            ServerPlayer player,
            double previousX,
            double previousY,
            double previousZ,
            double currentX,
            double currentY,
            double currentZ) {

        AABB currentBox = player.getBoundingBox();
        AABB previousBox = currentBox.move(
                previousX - currentX,
                previousY - currentY,
                previousZ - currentZ);

        AABB searchBox = previousBox
                .minmax(currentBox)
                .inflate(COLLISION_SEARCH_INFLATION);

        double halfWidth = player.getBbWidth() * 0.5D;
        double halfHeight = player.getBbHeight() * 0.5D;

        Vec3 start = new Vec3(
                previousX,
                previousY + halfHeight,
                previousZ);
        Vec3 end = new Vec3(
                currentX,
                currentY + halfHeight,
                currentZ);

        List<LivingEntity> candidates =
                player.serverLevel().getEntitiesOfClass(
                        LivingEntity.class,
                        searchBox,
                        target -> isValidChargeTarget(player, target));

        LivingEntity closest = null;
        double closestDistanceSqr = Double.MAX_VALUE;

        for (LivingEntity target : candidates) {
            AABB expandedTarget = target.getBoundingBox().inflate(
                    halfWidth + COLLISION_EPSILON,
                    halfHeight + COLLISION_EPSILON,
                    halfWidth + COLLISION_EPSILON);

            Optional<Vec3> clipped = expandedTarget.clip(start, end);
            Vec3 contactPoint;

            if (clipped.isPresent()) {
                contactPoint = clipped.get();
            } else if (currentBox
                    .inflate(COLLISION_EPSILON)
                    .intersects(target.getBoundingBox())) {
                contactPoint = end;
            } else {
                continue;
            }

            double contactDistanceSqr = start.distanceToSqr(contactPoint);
            if (contactDistanceSqr < closestDistanceSqr) {
                closestDistanceSqr = contactDistanceSqr;
                closest = target;
            }
        }

        return closest;
    }

    private static boolean isValidChargeTarget(
            ServerPlayer player,
            LivingEntity target) {

        if (target == null
                || target == player
                || !target.isAlive()
                || target.isDeadOrDying()
                || target.isRemoved()) {
            return false;
        }

        // Scoreboard teams and tameable/owned allies are respected through
        // Minecraft's bilateral alliance checks.
        if (player.isAlliedTo(target) || target.isAlliedTo(player)) {
            return false;
        }

        if (target instanceof Player otherPlayer) {
            if (otherPlayer.isCreative()
                    || otherPlayer.isSpectator()
                    || !player.canHarmPlayer(otherPlayer)) {
                return false;
            }
        }

        return true;
    }

    private static void performChargeImpact(
            ServerPlayer player,
            LivingEntity target,
            int momentumLevel,
            double deltaX,
            double deltaZ) {

        int tierIndex = Math.max(
                0,
                Math.min(
                        momentumLevel - 1,
                        IMPACT_STUN_DURATION_TICKS.length - 1));

        StunService.apply(
                target,
                player,
                IMPACT_STUN_DURATION_TICKS[tierIndex]);

        Vec3 horizontalDirection = new Vec3(deltaX, 0.0D, deltaZ);
        if (horizontalDirection.lengthSqr() < 1.0E-6D) {
            Vec3 look = player.getLookAngle();
            horizontalDirection = new Vec3(look.x, 0.0D, look.z);
        }

        if (horizontalDirection.lengthSqr() >= 1.0E-6D) {
            horizontalDirection = horizontalDirection.normalize();

            // LivingEntity#knockback subtracts the supplied X/Z vector, so
            // negate the movement direction to launch the target forward.
            target.knockback(
                    IMPACT_KNOCKBACK_STRENGTH[tierIndex],
                    -horizontalDirection.x,
                    -horizontalDirection.z);
        }

        player.serverLevel().playSound(
                null,
                target.blockPosition(),
                SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                SoundSource.PLAYERS,
                1.0F,
                0.9F + (0.05F * tierIndex));
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
        private double lastY;
        private double lastZ;
        private double distance;
        private int decayTicks;

        private ChargeState(
                double lastX,
                double lastY,
                double lastZ) {
            this.lastX = lastX;
            this.lastY = lastY;
            this.lastZ = lastZ;
        }
    }
}
