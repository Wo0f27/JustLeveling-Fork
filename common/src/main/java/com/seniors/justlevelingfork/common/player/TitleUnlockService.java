package com.seniors.justlevelingfork.common.player;

import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class TitleUnlockService {
    private static BiConsumer<ServerPlayer, String> sender = (player, titleName) -> {};

    private TitleUnlockService() {
    }

    public static void setSender(BiConsumer<ServerPlayer, String> sender) {
        TitleUnlockService.sender = Optional.ofNullable(sender).orElse((player, titleName) -> {});
    }

    public static void send(ServerPlayer player, String titleName) {
        if (player == null || titleName == null || titleName.isBlank()) {
            return;
        }

        sender.accept(player, titleName);
    }
}
