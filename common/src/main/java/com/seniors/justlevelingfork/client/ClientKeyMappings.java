package com.seniors.justlevelingfork.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.seniors.justlevelingfork.client.screen.AptitudesOverviewScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class ClientKeyMappings {
    public static final KeyMapping OPEN_APTITUDES = new KeyMapping(
            "key.justlevelingfork.open_aptitudes",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "key.justlevelingfork.title");

    private ClientKeyMappings() {
    }

    public static void handleClientTick(Minecraft client) {
        if (client == null || client.player == null || client.level == null) {
            return;
        }

        while (OPEN_APTITUDES.consumeClick()) {
            client.setScreen(new AptitudesOverviewScreen());
        }
    }
}
