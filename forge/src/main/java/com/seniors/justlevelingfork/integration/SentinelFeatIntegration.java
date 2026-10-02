package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatCooldownService;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.registry.ForgeRegistryMobEffects;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import io.redspace.ironsspellbooks.util.ModTags;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Server-authoritative direct control and short-range ally protection for Sentinel. */
public final class SentinelFeatIntegration {

    public static final ResourceLocation SENTINEL =
            new ResourceLocation(Constants.MOD_ID, "sentinel");
    private static final ResourceLocation GUARDIAN_COOLDOWN =
            new ResourceLocation(Constants.MOD_ID, "sentinel_guardian");

    private static final int ROOT_LOCKOUT_TICKS = 40;
    private static final int GUARDIAN_COOLDOWN_TICKS = 40;
    private static final int DIRECT_ROOT_TICKS = 10;
    private static final int GUARDIAN_ROOT_TICKS = 20;
    private static final int DIRECT_STAGE_TICKS = 10;
    private static final int GUARDIAN_STAGE_III_TICKS = 15;
    private static final int GUARDIAN_STAGE_II_TICKS = 15;
    private static final int GUARDIAN_STAGE_I_TICKS = 10;
    private static final double GUARDIAN_RANGE = 4.0D;
    private static final double GUARDIAN_RANGE_SQR = GUARDIAN_RANGE * GUARDIAN_RANGE;

    /** No persistent data: keys are weak and finished/dead targets are removed each tick. */
    private static final Map<LivingEntity, ControlState> CONTROLS = new WeakHashMap<>();

    private SentinelFeatIntegration() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(SentinelFeatIntegration.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() <= 0.0F) {
            return;
        }

        applyDirectControl(event);
        applyGuardianControl(event);
    }

    private static void applyDirectControl(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer sentinel)
                || sentinel.isSpectator()
                || source.getDirectEntity() != sentinel
                || !source.is(DamageTypes.PLAYER_ATTACK)
                || !FeatProgressionService.hasFeat(sentinel, SENTINEL)
                || !isValidControlTarget(sentinel, event.getEntity())) {
            return;
        }

        applyControl(sentinel, event.getEntity(), false);
    }

    private static void applyGuardianControl(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker
                || !(attacker.level() instanceof ServerLevel level)
                || !attacker.isAlive()
                || attacker.isRemoved()) {
            return;
        }

        for (ServerPlayer sentinel : level.getEntitiesOfClass(
                ServerPlayer.class,
                attacker.getBoundingBox().inflate(GUARDIAN_RANGE))) {
            if (sentinel.distanceToSqr(attacker) > GUARDIAN_RANGE_SQR
                    || sentinel == victim
                    || sentinel.isSpectator()
                    || !FeatProgressionService.hasFeat(sentinel, SENTINEL)
                    || !isProtectedAlly(sentinel, victim)
                    || !isValidGuardianAttacker(sentinel, attacker, victim)
                    || !FeatCooldownService.tryStart(
                            sentinel, GUARDIAN_COOLDOWN, GUARDIAN_COOLDOWN_TICKS)) {
                continue;
            }

            applyControl(sentinel, attacker, true);
        }
    }

    private static boolean isProtectedAlly(ServerPlayer sentinel, LivingEntity victim) {
        if (victim == sentinel || victim.isRemoved()) {
            return false;
        }

        if (sentinel.isAlliedTo(victim) || victim.isAlliedTo(sentinel)) {
            return true;
        }

        return victim instanceof TamableAnimal tameable
                && sentinel.getUUID().equals(tameable.getOwnerUUID());
    }

    private static boolean isValidGuardianAttacker(
            ServerPlayer sentinel, LivingEntity attacker, LivingEntity victim) {
        if (!isValidControlTarget(sentinel, attacker)) {
            return false;
        }

        if (attacker instanceof Player player) {
            return !(victim instanceof Player targetPlayer)
                    || player.canHarmPlayer(targetPlayer);
        }

        return attacker instanceof Enemy
                || attacker.getType().getCategory() == MobCategory.MONSTER
                || (attacker instanceof Mob mob
                    && (mob.getTarget() == victim || mob.getTarget() == sentinel));
    }

    private static boolean isValidControlTarget(ServerPlayer sentinel, LivingEntity target) {
        if (target == sentinel
                || !target.isAlive()
                || target.isDeadOrDying()
                || target.isRemoved()
                || sentinel.isAlliedTo(target)
                || target.isAlliedTo(sentinel)) {
            return false;
        }

        return !(target instanceof Player player)
                || (!player.isCreative()
                    && !player.isSpectator()
                    && sentinel.canHarmPlayer(player));
    }

    private static void applyControl(
            ServerPlayer sentinel, LivingEntity target, boolean guardian) {
        long now = sentinel.getServer().overworld().getGameTime();
        ControlState state = CONTROLS.computeIfAbsent(target, ignored -> new ControlState());

        // Direct hits cannot downgrade a still-active Guardian Hinder sequence.
        boolean guardianTiming = guardian
                || (state.guardianTiming
                    && (state.awaitingOwnRoot || now < state.hinderEndTick));
        state.guardianTiming = guardianTiming;

        if (guardian || now >= state.rootLockoutEndTick) {
            int rootTicks = guardian ? GUARDIAN_ROOT_TICKS : DIRECT_ROOT_TICKS;
            RootEntity root = tryCreateRoot(sentinel, target, rootTicks);
            if (root != null) {
                state.ownRootUuid = root.getUUID();
                state.awaitingOwnRoot = true;
                state.rootMinimumEndTick = now + rootTicks;
                state.rootLockoutEndTick = now + ROOT_LOCKOUT_TICKS;
            }
        }

        target.removeEffect(ForgeRegistryMobEffects.SENTINEL_HINDER.get());
        state.appliedHinderAmplifier = -1;
        if (shouldWaitForOwnRoot(target, state, now)) {
            // Iron's entity decides when the mount actually ends. Do not spend
            // any Hinder ticks while Sentinel's own root is still present.
            state.hinderEndTick = Long.MAX_VALUE;
        } else {
            state.awaitingOwnRoot = false;
            state.ownRootUuid = null;
            scheduleHinder(state, now);
        }
        syncHinder(target, state, now);
    }

    private static RootEntity tryCreateRoot(
            ServerPlayer sentinel, LivingEntity target, int durationTicks) {
        if (!target.isAlive()
                || target.isDeadOrDying()
                || target.isRemoved()
                || target.level().isClientSide
                || target.getType().is(ModTags.CANT_ROOT)
                || target.getVehicle() instanceof RootEntity) {
            return null;
        }

        RootEntity root = new RootEntity(target.level(), sentinel);
        root.setDuration(durationTicks);
        root.setTarget(target);
        root.moveTo(target.position());
        if (!target.level().addFreshEntity(root)) {
            return null;
        }

        target.stopRiding();
        if (!target.startRiding(root, true)) {
            root.discard();
            return null;
        }
        return root;
    }

    private static boolean isRidingOwnRoot(LivingEntity target, ControlState state) {
        return state.awaitingOwnRoot
                && state.ownRootUuid != null
                && target.getVehicle() instanceof RootEntity root
                && !root.isRemoved()
                && state.ownRootUuid.equals(root.getUUID());
    }

    private static boolean shouldWaitForOwnRoot(
            LivingEntity target, ControlState state, long now) {
        if (!state.awaitingOwnRoot || target.getType().is(ModTags.CANT_ROOT)) {
            return false;
        }
        if (target.getVehicle() instanceof RootEntity) {
            return isRidingOwnRoot(target, state);
        }
        return now < state.rootMinimumEndTick;
    }

    private static void scheduleHinder(ControlState state, long now) {
        state.hinderStartTick = now;
        state.stageIIIEndTick = now
                + (state.guardianTiming ? GUARDIAN_STAGE_III_TICKS : DIRECT_STAGE_TICKS);
        state.stageIIEndTick = state.stageIIIEndTick
                + (state.guardianTiming ? GUARDIAN_STAGE_II_TICKS : DIRECT_STAGE_TICKS);
        state.hinderEndTick = state.stageIIEndTick
                + (state.guardianTiming ? GUARDIAN_STAGE_I_TICKS : DIRECT_STAGE_TICKS);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || CONTROLS.isEmpty()) {
            return;
        }

        long now = event.getServer().overworld().getGameTime();
        Iterator<Map.Entry<LivingEntity, ControlState>> iterator =
                CONTROLS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, ControlState> entry = iterator.next();
            LivingEntity target = entry.getKey();
            ControlState state = entry.getValue();
            if (target == null
                    || !target.isAlive()
                    || target.isRemoved()
                    || target.level().getServer() != event.getServer()) {
                iterator.remove();
                continue;
            }

            syncHinder(target, state, now);
            if (now >= state.rootLockoutEndTick && now >= state.hinderEndTick) {
                iterator.remove();
            }
        }
    }

    private static void syncHinder(LivingEntity target, ControlState state, long now) {
        if (state.awaitingOwnRoot) {
            if (shouldWaitForOwnRoot(target, state, now)) {
                return;
            }
            state.awaitingOwnRoot = false;
            state.ownRootUuid = null;
            scheduleHinder(state, now);
        }

        int amplifier = now < state.hinderStartTick || now >= state.hinderEndTick
                ? -1
                : now < state.stageIIIEndTick ? 2
                : now < state.stageIIEndTick ? 1 : 0;
        if (amplifier == state.appliedHinderAmplifier) {
            return;
        }

        target.removeEffect(ForgeRegistryMobEffects.SENTINEL_HINDER.get());
        state.appliedHinderAmplifier = amplifier;
        if (amplifier < 0) {
            return;
        }

        long stageEnd = amplifier == 2 ? state.stageIIIEndTick
                : amplifier == 1 ? state.stageIIEndTick : state.hinderEndTick;
        target.addEffect(new MobEffectInstance(
                ForgeRegistryMobEffects.SENTINEL_HINDER.get(),
                (int) (stageEnd - now), amplifier, false, false, false));
    }

    private static final class ControlState {
        private long rootLockoutEndTick;
        private long rootMinimumEndTick;
        private UUID ownRootUuid;
        private boolean awaitingOwnRoot;
        private long hinderStartTick;
        private long stageIIIEndTick;
        private long stageIIEndTick;
        private long hinderEndTick;
        private int appliedHinderAmplifier = -1;
        private boolean guardianTiming;
    }
}
