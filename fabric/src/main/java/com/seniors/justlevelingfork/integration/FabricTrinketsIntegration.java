package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.common.integration.AccessoryRestrictions;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FabricTrinketsIntegration {
    private FabricTrinketsIntegration() {
    }

    public static void dropLockedAccessories(ServerPlayer player) {
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> component.forEach((slot, stack) -> {
            if (AccessoryRestrictions.shouldDropEquipped(player, stack)) {
                AccessoryRestrictions.drop(player, stack);
                slot.inventory().setItem(slot.index(), ItemStack.EMPTY);
            }
        }));
    }
}
