package com.seniors.justlevelingfork.integration;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public final class ForgeIntegrationConfig {
    private static BooleanSupplier logTaczGunNames = () -> false;
    private static BooleanSupplier logSpellIds = () -> false;

    private ForgeIntegrationConfig() {
    }

    public static void setLogTaczGunNames(BooleanSupplier logTaczGunNames) {
        ForgeIntegrationConfig.logTaczGunNames = Optional.ofNullable(logTaczGunNames).orElse(() -> false);
    }

    public static boolean logTaczGunNames() {
        return logTaczGunNames.getAsBoolean();
    }

    public static void setLogSpellIds(BooleanSupplier logSpellIds) {
        ForgeIntegrationConfig.logSpellIds = Optional.ofNullable(logSpellIds).orElse(() -> false);
    }

    public static boolean logSpellIds() {
        return logSpellIds.getAsBoolean();
    }
}
