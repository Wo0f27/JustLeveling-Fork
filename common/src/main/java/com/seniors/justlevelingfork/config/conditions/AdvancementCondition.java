package com.seniors.justlevelingfork.config.conditions;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.config.models.TitleModel;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCondition extends ConditionImpl<Boolean> {
    public AdvancementCondition() {
        super("Advancement");
    }

    @Override
    public void processVariable(String value, ServerPlayer serverPlayer) {
        Advancement advancement = serverPlayer.getServer()
                .getAdvancements()
                .getAdvancement(new ResourceLocation(value.replace("-", "/")));
        if (advancement == null) {
            Constants.LOG.error(">> Error! Advancement name {} not found!", value);
            setProcessedValue(false);
            return;
        }

        setProcessedValue(serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone());
    }

    @Override
    public boolean meetCondition(String value, TitleModel.EComparator comparator) {
        return getProcessedValue();
    }
}
