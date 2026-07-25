package com.seniors.justlevelingfork.common;

import java.util.Optional;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HorseEquipmentRestrictions {
    private static ItemUseRestriction restriction = (player, stack) -> true;

    private HorseEquipmentRestrictions() {
    }

    public static void setRestriction(ItemUseRestriction restriction) {
        HorseEquipmentRestrictions.restriction = Optional.ofNullable(restriction).orElse((player, stack) -> true);
    }

    public static boolean canEquip(Player player, AbstractHorse horse, ItemStack stack) {
        if (player == null || player.isCreative() || stack.isEmpty()) {
            return true;
        }
        if (!stack.is(Items.SADDLE) && !horse.isArmor(stack)) {
            return true;
        }

        return restriction.canUseItem(player, stack);
    }

    @FunctionalInterface
    public interface ItemUseRestriction {
        boolean canUseItem(Player player, ItemStack stack);
    }
}
