package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.client.gui.ClientOverlayState;

final class ForgeAptitudeWarningClientHandler {
    private ForgeAptitudeWarningClientHandler() {
    }

    static void show(String restrictionId) {
        ClientOverlayState.showAptitudeWarning(restrictionId);
    }
}
