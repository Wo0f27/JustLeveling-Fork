package com.seniors.justlevelingfork.registry.skills;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ConvergenceSkill {
    private ConvergenceSkill() {
    }

    public static ItemStack drop(ServerPlayer player, ItemStack craftedStack) {
        if (player == null
                || craftedStack.isEmpty()
                || !PlayerProgressService.isSkillEnabled(player, RegistrySkills.CONVERGENCE)
                || !passesChance()) {
            return ItemStack.EMPTY;
        }

        return drop(craftedStack);
    }

    public static ItemStack drop(ItemStack craftedStack) {
        for (ItemDrop drop : getItems()) {
            if (drop.craftingItem() != null
                    && drop.convergenceItem() != null
                    && craftedStack.is(drop.craftingItem())) {
                return drop.convergenceItem().getDefaultInstance();
            }
        }

        return ItemStack.EMPTY;
    }

    public static List<ItemDrop> getItems() {
        List<ItemDrop> drops = new ArrayList<>();
        for (String entry : CommonConfigService.convergenceItems()) {
            parseEntry(entry).ifPresent(drops::add);
        }
        return drops;
    }

    private static boolean passesChance() {
        int chance = Math.max(1, (int) RegistrySkills.CONVERGENCE.getValue()[0]);
        return chance >= 100 || ThreadLocalRandom.current().nextInt(chance) == 1;
    }

    private static Optional<ItemDrop> parseEntry(String entry) {
        if (entry == null || entry.isBlank() || !entry.contains("#")) {
            return Optional.empty();
        }

        String[] parts = entry.split("#", 2);
        Item craftingItem = BuiltInRegistries.ITEM.get(new ResourceLocation(parts[0]));
        Item convergenceItem = BuiltInRegistries.ITEM.get(new ResourceLocation(parts[1]));
        if (craftingItem == Items.AIR || convergenceItem == Items.AIR) {
            return Optional.empty();
        }

        return Optional.of(new ItemDrop(craftingItem, convergenceItem));
    }

    public record ItemDrop(Item craftingItem, Item convergenceItem) {
    }
}
