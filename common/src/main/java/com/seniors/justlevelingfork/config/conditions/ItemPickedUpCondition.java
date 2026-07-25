package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class ItemPickedUpCondition extends RegistryStatCondition<Item> {
    public ItemPickedUpCondition() {
        super("ItemPickedUp", Stats.ITEM_PICKED_UP, "Item");
    }
}
