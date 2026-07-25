package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.registry.skills.ConvergenceSkill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class MixResultSlot {
    @Inject(method = "onTake", at = @At("TAIL"))
    private void justlevelingfork$applyConvergence(Player player, ItemStack craftedStack, CallbackInfo callbackInfo) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack convergenceItem = ConvergenceSkill.drop(serverPlayer, craftedStack);
            if (!convergenceItem.isEmpty()) {
                player.drop(convergenceItem, false);
            }
        }
    }
}
