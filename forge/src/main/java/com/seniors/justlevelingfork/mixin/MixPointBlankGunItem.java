package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.event.ClientLockItemRestrictions;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.vicmatskiv.pointblank.item.GunItem", remap = false)
public class MixPointBlankGunItem {
    @Inject(method = "tryFire", at = @At("HEAD"), cancellable = true, remap = false)
    private void justlevelingfork$tryFire(
            LocalPlayer player, ItemStack itemStack, Entity targetEntity, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!player.isCreative() && !ClientLockItemRestrictions.canUseItem(itemStack)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
