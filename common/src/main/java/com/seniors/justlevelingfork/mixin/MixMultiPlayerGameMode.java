package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.registry.RegistryAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixMultiPlayerGameMode {
    private static final float JUSTLEVELINGFORK_MAX_VANILLA_SERVER_REACH = 6.0F;

    @Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
    private void justlevelingfork$getPickRange(CallbackInfoReturnable<Float> callbackInfo) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        double reach = Math.max(
                player.getAttributeValue(RegistryAttributes.BLOCK_REACH),
                player.getAttributeValue(RegistryAttributes.ENTITY_REACH));
        if (reach <= 0.0D) {
            return;
        }

        callbackInfo.setReturnValue(Math.min(
                callbackInfo.getReturnValue() + (float) reach,
                JUSTLEVELINGFORK_MAX_VANILLA_SERVER_REACH));
    }

    @Inject(method = "hasFarPickRange", at = @At("RETURN"), cancellable = true)
    private void justlevelingfork$hasFarPickRange(CallbackInfoReturnable<Boolean> callbackInfo) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.getAttributeValue(RegistryAttributes.ENTITY_REACH) > 0.0D) {
            callbackInfo.setReturnValue(true);
        }
    }
}
