package com.seniors.justlevelingfork.client;

import com.seniors.justlevelingfork.client.ClientKeyMappings;
import com.seniors.justlevelingfork.client.gui.ClientOverlayState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class FabricClientEvents {
    private FabricClientEvents() {
    }

    public static void load() {
        KeyBindingHelper.registerKeyBinding(ClientKeyMappings.OPEN_APTITUDES);
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> ClientOverlayState.render(graphics));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientOverlayState.tick();
            ClientKeyMappings.handleClientTick(client);
        });
    }
}
