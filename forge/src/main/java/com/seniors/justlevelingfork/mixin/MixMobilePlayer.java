package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.integration.MobileFeatIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixMobilePlayer extends LivingEntity {

    protected MixMobilePlayer(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
    private void justlevelingfork$mobileSoulTerrain(CallbackInfoReturnable<Float> callback) {
        Player player = (Player) (Object) this;
        if (callback.getReturnValue() >= 1.0F
                || !MobileFeatIntegration.hasMobile(player)) {
            return;
        }

        BlockState atFeet = player.level().getBlockState(player.blockPosition());
        if (atFeet.getBlock().getSpeedFactor() < 1.0F
                && !atFeet.is(BlockTags.SOUL_SPEED_BLOCKS)) {
            // Do not erase a different block's slowdown above Soul Sand.
            return;
        }

        BlockPos below = getBlockPosBelowThatAffectsMyMovement();
        BlockState ground = player.level().getBlockState(below);
        if (ground.is(BlockTags.SOUL_SPEED_BLOCKS)) {
            // Respect a higher factor supplied by another mod or Soul Speed.
            callback.setReturnValue(1.0F);
        }
    }
}
