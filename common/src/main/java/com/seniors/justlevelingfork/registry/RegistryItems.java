package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.item.SkillResetItem;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class RegistryItems {
    private static final Map<String, Supplier<Item>> ITEM_FACTORIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Item> ITEMS_BY_ID = new LinkedHashMap<>();

    static {
        register("leveling_book", () -> new Item(new Item.Properties()));
        register("skill_reset_crystal", () -> new SkillResetItem(new Item.Properties().stacksTo(1)));
    }

    private RegistryItems() {
    }

    public static Collection<Item> values() {
        ensureLoaded();
        return ITEMS_BY_ID.values();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }

    public static ResourceLocation getId(Item item) {
        ensureLoaded();
        return ITEMS_BY_ID.entrySet().stream()
                .filter(entry -> entry.getValue() == item)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + item));
    }

    public static Item getItem(String name) {
        ensureLoaded();
        return ITEMS_BY_ID.get(id(name.toLowerCase(Locale.ROOT)));
    }

    private static void register(String name, Supplier<Item> factory) {
        ITEM_FACTORIES.put(name, factory);
    }

    private static void ensureLoaded() {
        if (!ITEMS_BY_ID.isEmpty()) {
            return;
        }

        ITEM_FACTORIES.forEach((name, factory) -> ITEMS_BY_ID.put(id(name), factory.get()));
    }
}
