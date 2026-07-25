package com.seniors.justlevelingfork.common.player;

import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class AptitudeWarningService {
    private static BiConsumer<ServerPlayer, String> sender = (player, restrictionId) -> {};

    private AptitudeWarningService() {
    }

    public static void setSender(BiConsumer<ServerPlayer, String> sender) {
        AptitudeWarningService.sender = Optional.ofNullable(sender).orElse((player, restrictionId) -> {});
    }

    public static void send(ServerPlayer player, String restrictionId) {
        if (player == null || restrictionId == null || restrictionId.isBlank()) {
            return;
        }

        sender.accept(player, restrictionId);
    }
}
