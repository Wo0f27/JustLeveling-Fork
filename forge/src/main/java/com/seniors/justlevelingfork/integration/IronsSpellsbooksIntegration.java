package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IronsSpellsbooksIntegration {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSpellCastEvent(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String spellId = event.getSpellId();
        if (ForgeIntegrationConfig.logSpellIds()) {
            player.sendSystemMessage(Component.literal(String.format("[JLFork] >> Spell ID: %s", spellId)));
        }

        if (!player.isCreative() && !HandlerAptitude.canUseSpecificId(player, spellId)) {
            event.setCanceled(true);
        }
    }
}
