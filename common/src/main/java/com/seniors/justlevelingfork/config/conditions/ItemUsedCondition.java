package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class ItemUsedCondition extends RegistryStatCondition<Item> {
    public ItemUsedCondition() {
        super("ItemUsed", Stats.ITEM_USED, "Item");
    }
}
