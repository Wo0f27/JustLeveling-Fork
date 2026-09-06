package com.seniors.justlevelingfork.common.feat.effect;

import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.feat.FeatEffectDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import net.minecraft.server.level.ServerPlayer;

/**
 * Marker effect for feats whose gameplay is implemented by an external
 * event/service that checks feat ownership at runtime.
 *
 * The feat system requires every selectable feat to have a registered
 * effect. This marker deliberately applies no immediate mutation.
 */
public final class PassiveFeatEffect implements FeatEffect {

    public static final PassiveFeatEffect INSTANCE =
            new PassiveFeatEffect();

    private PassiveFeatEffect() {
    }

    @Override
    public boolean canApply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {

        return player != null
                && progress != null
                && feat != null
                && effect != null;
    }

    @Override
    public void apply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {
        // Intentionally empty. Runtime services derive behaviour from
        // FeatProgressionService.hasFeat(...).
    }
}
