package com.seniors.justlevelingfork.common.platform;

import com.seniors.justlevelingfork.registry.passive.Passive;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;

public final class ReachAttributeBridge {
    private static TriConsumer<Player, Passive, Integer> applier = (player, passive, passiveLevel) -> {};

    private ReachAttributeBridge() {
    }

    public static void setApplier(TriConsumer<Player, Passive, Integer> applier) {
        ReachAttributeBridge.applier = Optional.ofNullable(applier).orElse((player, passive, passiveLevel) -> {});
    }

    public static void apply(Player player, Passive passive, int passiveLevel) {
        applier.accept(player, passive, passiveLevel);
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A first, B second, C third);
    }
}
