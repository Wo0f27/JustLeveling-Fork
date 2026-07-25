package com.seniors.justlevelingfork.registry.skills;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TreasureHunterSkill {
    private TreasureHunterSkill() {
    }

    public static ItemStack drop() {
        int chance = Math.max(1, (int) RegistrySkills.TREASURE_HUNTER.getValue()[0]);
        List<List<BlockDrop>> dropLists = getItems();
        if (dropLists.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int listIndex = ThreadLocalRandom.current().nextInt(chance);
        if (listIndex >= dropLists.size()) {
            return ItemStack.EMPTY;
        }

        List<BlockDrop> drops = dropLists.get(listIndex);
        if (drops.isEmpty()) {
            return ItemStack.EMPTY;
        }

        BlockDrop drop = drops.get(ThreadLocalRandom.current().nextInt(drops.size()));
        ItemStack stack = drop.item().getDefaultInstance();
        stack.setTag(drop.tag().copy());
        return stack;
    }

    public static List<List<BlockDrop>> getItems() {
        List<List<BlockDrop>> dropList = new ArrayList<>();
        for (String entry : CommonConfigService.treasureHunterItems()) {
            List<BlockDrop> drops = parseEntry(entry);
            if (!drops.isEmpty()) {
                dropList.add(drops);
            }
        }
        return dropList;
    }

    private static List<BlockDrop> parseEntry(String entry) {
        if (entry == null || entry.isBlank()) {
            return List.of();
        }

        int listStart = entry.indexOf('[');
        if (listStart >= 0 && entry.endsWith("]")) {
            String listValue = entry.substring(listStart + 1, entry.length() - 1);
            List<BlockDrop> drops = new ArrayList<>();
            for (String itemEntry : listValue.split(";")) {
                parseDrop(itemEntry).ifPresent(drops::add);
            }
            return drops;
        }

        return parseDrop(entry).map(List::of).orElse(List.of());
    }

    private static java.util.Optional<BlockDrop> parseDrop(String entry) {
        if (entry == null || entry.isBlank()) {
            return java.util.Optional.empty();
        }

        CompoundTag tag = new CompoundTag();
        String itemId = entry;
        int nbtStart = entry.indexOf('{');
        if (nbtStart >= 0 && entry.endsWith("}")) {
            itemId = entry.substring(0, nbtStart);
            try {
                tag = TagParser.parseTag(entry.substring(nbtStart));
            } catch (CommandSyntaxException exception) {
                throw new JsonSyntaxException("Invalid NBT Entry: " + exception);
            }
        }

        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        return item == net.minecraft.world.item.Items.AIR
                ? java.util.Optional.empty()
                : java.util.Optional.of(new BlockDrop(item, tag));
    }

    public record BlockDrop(Item item, CompoundTag tag) {
    }
}
