package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class MixVillager {
    @Unique
    private final Villager justlevelingfork$villager = (Villager) (Object) this;

    @Inject(
            method = "updateSpecialPrices(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("TAIL"))
    private void justlevelingfork$updateSpecialPrices(Player player, CallbackInfo callbackInfo) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.HAGGLER)) {
            return;
        }

        for (MerchantOffer offer : this.justlevelingfork$villager.getOffers()) {
            double discount = RegistrySkills.HAGGLER.getValue()[0] / 100.0D;
            int amount = (int) Math.floor(discount * offer.getBaseCostA().getCount());
            offer.addToSpecialPriceDiff(-Math.max(amount, 1));
        }
    }
}
