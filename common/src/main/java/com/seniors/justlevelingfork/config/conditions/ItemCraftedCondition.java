package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class ItemCraftedCondition extends RegistryStatCondition<Item> {
    public ItemCraftedCondition() {
        super("ItemCrafted", Stats.ITEM_CRAFTED, "Item");
    }
}
