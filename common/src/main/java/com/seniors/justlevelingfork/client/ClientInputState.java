package com.seniors.justlevelingfork.client;

import net.minecraft.client.gui.screens.Screen;

public final class ClientInputState {
    private ClientInputState() {
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }
}
