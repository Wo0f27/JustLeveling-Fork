package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixServerPlayer {
    @Unique
    private final ServerPlayer justlevelingfork$player = (ServerPlayer) (Object) this;

    @Unique
    private int justlevelingfork$respawnSyncTicks;

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void justlevelingfork$copyProgressAfterRespawn(ServerPlayer oldPlayer, boolean alive, CallbackInfo callbackInfo) {
        PlayerProgressService.get(oldPlayer).ifPresent(oldProgress ->
                PlayerProgressService.get(this.justlevelingfork$player).ifPresent(progress -> {
                    progress.copyFrom(oldProgress);
                    PlayerProgressService.refreshPassiveModifiers(this.justlevelingfork$player);
                    PlayerProgressService.sync(this.justlevelingfork$player);
                    this.justlevelingfork$respawnSyncTicks = 100;
                }));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void justlevelingfork$syncProgressAfterRespawn(CallbackInfo callbackInfo) {
        if (this.justlevelingfork$respawnSyncTicks <= 0) {
            return;
        }

        if (this.justlevelingfork$respawnSyncTicks == 100
                || this.justlevelingfork$respawnSyncTicks == 1
                || this.justlevelingfork$respawnSyncTicks % 10 == 0) {
            PlayerProgressService.sync(this.justlevelingfork$player);
        }
        this.justlevelingfork$respawnSyncTicks--;
    }
}
