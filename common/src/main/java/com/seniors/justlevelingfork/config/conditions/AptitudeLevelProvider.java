package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface AptitudeLevelProvider {
    int getAptitudeLevel(ServerPlayer serverPlayer, String aptitudeName);
}
