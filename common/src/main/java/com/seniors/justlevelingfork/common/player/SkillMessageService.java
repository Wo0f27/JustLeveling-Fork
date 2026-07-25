package com.seniors.justlevelingfork.common.player;

import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class SkillMessageService {
    private static BiConsumer<ServerPlayer, SkillMessage> sender = (player, message) -> {};

    private SkillMessageService() {
    }

    public static void setSender(BiConsumer<ServerPlayer, SkillMessage> sender) {
        SkillMessageService.sender = Optional.ofNullable(sender).orElse((player, message) -> {});
    }

    public static void send(ServerPlayer player, String translationKey, int amount) {
        if (player == null || translationKey == null || translationKey.isBlank()) {
            return;
        }

        sender.accept(player, new SkillMessage(translationKey, amount));
    }

    public record SkillMessage(String translationKey, int amount) {
    }
}
