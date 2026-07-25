package com.seniors.justlevelingfork.integration;

import com.mrcrayfish.guns.event.GunFireEvent;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CrayfishGunModIntegration {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGunFireEvent(GunFireEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isCreative()) {
            return;
        }

        ItemStack itemStack = event.getStack();
        if (!HandlerAptitude.canUseItem(player, itemStack) || !ForgeModularItemRestrictions.canUse(player, itemStack)) {
            event.setCanceled(true);
        }
    }
}
