package com.seniors.justlevelingfork.common.integration;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerPlayer;

public final class AptitudeLevelUpEvents {
    private static BiPredicate<ServerPlayer, Aptitude> cancellationHook = (player, aptitude) -> false;

    private AptitudeLevelUpEvents() {
    }

    public static void setCancellationHook(BiPredicate<ServerPlayer, Aptitude> cancellationHook) {
        AptitudeLevelUpEvents.cancellationHook =
                Optional.ofNullable(cancellationHook).orElse((player, aptitude) -> false);
    }

    public static boolean isCancelled(ServerPlayer player, Aptitude aptitude) {
        return cancellationHook.test(player, aptitude);
    }
}
