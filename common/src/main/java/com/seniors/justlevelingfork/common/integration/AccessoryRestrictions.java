package com.seniors.justlevelingfork.common.integration;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AccessoryRestrictions {
    private AccessoryRestrictions() {
    }

    public static boolean canEquip(ServerPlayer player, ItemStack stack) {
        return player == null || player.isCreative() || HandlerAptitude.canUseItem(player, stack);
    }

    public static boolean shouldDropEquipped(ServerPlayer player, ItemStack stack) {
        return player != null && !player.isCreative() && !stack.isEmpty() && !HandlerAptitude.canUseItem(player, stack);
    }

    public static void drop(ServerPlayer player, ItemStack stack) {
        if (player == null || stack.isEmpty()) {
            return;
        }

        player.drop(stack.copy(), false);
        stack.setCount(0);
    }
}
