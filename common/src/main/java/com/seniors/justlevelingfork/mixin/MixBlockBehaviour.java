package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.event.MiningSkillEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class MixBlockBehaviour {
    @Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
    private void justlevelingfork$modifyObsidianDestroyProgress(
            BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> callbackInfo) {
        callbackInfo.setReturnValue(
                MiningSkillEffects.adjustObsidianDestroyProgress(player, state, callbackInfo.getReturnValue()));
    }
}
