package com.evandev.triggers.event.events;

import net.minecraft.world.entity.player.Player;

public class TriggerPlayerEvent {
    public final Player player;

    public TriggerPlayerEvent(Player player) {
        this.player = player;
    }

    public static class Tick extends TriggerPlayerEvent {
        public Tick(Player player) {
            super(player);
        }
    }
}
