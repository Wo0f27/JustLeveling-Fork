package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public abstract class MixPowderSnowBlock {
    @Inject(
            method = "canEntityWalkOnPowderSnow(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void justlevelingfork$canEntityWalkOnPowderSnow(
            Entity entity, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (entity instanceof ServerPlayer player
                && PlayerProgressService.isSkillEnabled(player, RegistrySkills.SNOW_WALKER)) {
            callbackInfo.setReturnValue(Boolean.TRUE);
        }
    }
}
