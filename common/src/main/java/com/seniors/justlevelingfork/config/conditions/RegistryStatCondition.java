package com.seniors.justlevelingfork.config.conditions;

import com.seniors.justlevelingfork.Constants;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;

abstract class RegistryStatCondition<T> extends IntegerCondition {
    private final StatType<T> statType;
    private final String valueKind;

    RegistryStatCondition(String conditionName, StatType<T> statType, String valueKind) {
        super(conditionName);
        this.statType = statType;
        this.valueKind = valueKind;
    }

    @Override
    public void processVariable(String value, ServerPlayer serverPlayer) {
        ResourceLocation statId = ResourceLocation.tryParse(value.toLowerCase(Locale.ROOT));
        if (statId == null) {
            Constants.LOG.error(">> Error! {} name {} not found!", valueKind, value);
            setProcessedValue(0);
            return;
        }

        statType.getRegistry().getOptional(statId).ifPresentOrElse(
                statValue -> setProcessedValue(serverPlayer.getStats().getValue(statType, statValue)),
                () -> {
                    Constants.LOG.error(">> Error! {} name {} not found!", valueKind, value);
                    setProcessedValue(0);
                });
    }
}
