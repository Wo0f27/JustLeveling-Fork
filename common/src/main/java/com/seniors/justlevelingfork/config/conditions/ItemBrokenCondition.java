package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class ItemBrokenCondition extends RegistryStatCondition<Item> {
    public ItemBrokenCondition() {
        super("ItemBroken", Stats.ITEM_BROKEN, "Item");
    }
}
