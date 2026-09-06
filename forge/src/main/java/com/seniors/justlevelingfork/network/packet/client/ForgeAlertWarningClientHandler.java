package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.client.gui.AlertClientOverlayState;

final class ForgeAlertWarningClientHandler {

    private ForgeAlertWarningClientHandler() {
    }

    static void show(int hostileCount) {
        AlertClientOverlayState.show(hostileCount);
    }
}
