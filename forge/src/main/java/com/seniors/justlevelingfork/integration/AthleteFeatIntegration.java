package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.w0of26.martialspells.prone.ProneRecoveryModifierService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.w0of26.martialspells.prone.ProneDurationModifierService;

public final class AthleteFeatIntegration {

    public static final ResourceLocation ATHLETE =
            new ResourceLocation(
                    Constants.MOD_ID,
                    "athlete"
            );

    private static final double
            HORIZONTAL_JUMP_MULTIPLIER =
            1.5D;

    private static final double
            PRONE_RESISTANCE_MULTIPLIER =
            0.5D;

    private AthleteFeatIntegration() {
    }

    public static void load() {

        /*
         * Martial Spells does not know what Athlete is.
         *
         * It simply asks all registered providers whether
         * this entity's post-Prone recovery should change.
         */
        ProneRecoveryModifierService.register(
                ATHLETE,
                entity -> {
                    if (!(entity instanceof ServerPlayer player)) {
                        return 1.0D;
                    }

                    return hasAthlete(player)
                            ? PRONE_RESISTANCE_MULTIPLIER
                            : 1.0D;
                }
        );

        ProneDurationModifierService.register(
                ATHLETE,
                entity -> {
                    if (!(entity instanceof ServerPlayer player)) {
                        return 1.0D;
                    }

                    return hasAthlete(player)
                            ? PRONE_RESISTANCE_MULTIPLIER
                            : 1.0D;
                }
        );

        MinecraftForge.EVENT_BUS.register(
                AthleteFeatIntegration.class
        );
    }

    @SubscribeEvent
    public static void onLivingJump(
            LivingEvent.LivingJumpEvent event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        if (!hasAthlete(player)) {
            return;
        }

        Vec3 movement =
                player.getDeltaMovement();

        /*
         * Increase horizontal momentum only.
         *
         * Y is deliberately untouched so Athlete
         * does not increase jump height.
         */
        player.setDeltaMovement(
                movement.x
                        * HORIZONTAL_JUMP_MULTIPLIER,
                movement.y,
                movement.z
                        * HORIZONTAL_JUMP_MULTIPLIER
        );

        /*
         * Ensure the server's changed velocity is
         * synchronized back to the player.
         */
        player.hurtMarked = true;
    }

    private static boolean hasAthlete(
            ServerPlayer player
    ) {

        return FeatProgressionService.hasFeat(
                player,
                ATHLETE
        );
    }
}