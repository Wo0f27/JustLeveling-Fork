package com.seniors.justlevelingfork.common.player;

import java.util.Optional;

public final class CharacterAdminClientRequests {

    private static Runnable accessRefreshSender =
            () -> {};

    private CharacterAdminClientRequests() {
    }

    public static void setAccessRefreshSender(
            Runnable sender) {

        accessRefreshSender =
                Optional.ofNullable(sender)
                        .orElse(() -> {});
    }

    public static void requestAccessRefresh() {
        accessRefreshSender.run();
    }
}