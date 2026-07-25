package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.client.gui.ClientOverlayState;

final class ForgeTitleUnlockClientHandler {
    private ForgeTitleUnlockClientHandler() {
    }

    static void show(String titleName) {
        ClientOverlayState.showTitleUnlock(titleName);
    }
}
