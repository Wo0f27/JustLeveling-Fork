package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TacZIntegration {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGunFireEvent(GunFireEvent event) {
        if (!(event.getShooter() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack itemStack = event.getGunItemStack();
        if (!(itemStack.getItem() instanceof ModernKineticGunItem gunItem)) {
            return;
        }

        ResourceLocation gunId = gunItem.getGunId(itemStack);
        if (ForgeIntegrationConfig.logTaczGunNames()) {
            player.sendSystemMessage(Component.literal(String.format("[JLFork] >> Gun ID: %s", gunId)));
        }

        if (!player.isCreative() && !HandlerAptitude.canUseItem(player, gunId)) {
            event.setCanceled(true);
        }
    }
}
