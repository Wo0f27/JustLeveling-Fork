package com.seniors.justlevelingfork.common.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AccessoryRestrictions {

    private AccessoryRestrictions() {
    }

    public static boolean canEquip(
            ServerPlayer player,
            ItemStack stack) {

        return true;
    }

    public static boolean shouldDropEquipped(
            ServerPlayer player,
            ItemStack stack) {

        return false;
    }

    public static void drop(
            ServerPlayer player,
            ItemStack stack) {

        // Legacy aptitude-based accessory restrictions retired.
    }
}