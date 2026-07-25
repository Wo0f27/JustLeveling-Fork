package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class ItemDroppedCondition extends RegistryStatCondition<Item> {
    public ItemDroppedCondition() {
        super("ItemDropped", Stats.ITEM_DROPPED, "Item");
    }
}
