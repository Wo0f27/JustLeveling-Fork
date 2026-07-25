package com.seniors.justlevelingfork;

import com.seniors.justlevelingfork.client.FabricClientEvents;
import com.seniors.justlevelingfork.network.FabricClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class JustLevelingFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientEvents.load();
        FabricClientNetworking.init();
    }
}
