package com.seniors.justlevelingfork.config.conditions;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.config.models.EAptitude;
import java.util.Locale;
import net.minecraft.server.level.ServerPlayer;

public class AptitudeCondition extends IntegerCondition {
    private final AptitudeLevelProvider aptitudeLevelProvider;

    public AptitudeCondition(AptitudeLevelProvider aptitudeLevelProvider) {
        super("Aptitude");
        this.aptitudeLevelProvider = aptitudeLevelProvider;
    }

    @Override
    public void processVariable(String value, ServerPlayer serverPlayer) {
        String aptitudeName = normalizeAptitudeName(value);
        try {
            EAptitude.valueOf(capitalize(aptitudeName));
        } catch (IllegalArgumentException e) {
            Constants.LOG.error(">> Error! Aptitude name {} not found!", value);
            setProcessedValue(0);
            return;
        }

        setProcessedValue(aptitudeLevelProvider.getAptitudeLevel(serverPlayer, aptitudeName));
    }

    private static String normalizeAptitudeName(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private static String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
