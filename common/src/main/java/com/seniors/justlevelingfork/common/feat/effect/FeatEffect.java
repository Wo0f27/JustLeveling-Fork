package com.seniors.justlevelingfork.common.feat.effect;

import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import net.minecraft.server.level.ServerPlayer;

public interface FeatEffect {

    /**
     * Validates whether this effect can currently be applied.
     *
     * choice is optional and depends on the effect type.
     * ASI uses it for strength/dexterity/etc.
     */
    boolean canApply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            String choice);

    /**
     * Applies the effect to PlayerProgress.
     *
     * This executes inside PlayerProgressService.update(...),
     * so derived attributes and client sync happen afterward.
     */
    void apply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            String choice);
}