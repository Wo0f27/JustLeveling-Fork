package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.common.config.ClientConfigService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

final class ForgeSkillMessageClientHandler {
    private ForgeSkillMessageClientHandler() {
    }

    static void show(String translationKey, int amount) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && ClientConfigService.shouldShowSkillMessage(translationKey)) {
            player.displayClientMessage(Component.translatable(translationKey, amount), true);
        }
    }
}
