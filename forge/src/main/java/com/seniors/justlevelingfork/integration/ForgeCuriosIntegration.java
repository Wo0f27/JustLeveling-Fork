package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.common.integration.AccessoryRestrictions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public final class ForgeCuriosIntegration {
    @SubscribeEvent
    public void onCurioEquip(CurioEquipEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player && !AccessoryRestrictions.canEquip(player, event.getStack())) {
            event.setResult(Event.Result.DENY);
        }
    }

    public static void dropLockedAccessories(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> curiosInventory.getCurios().forEach((id, slotInventory) -> {
            IDynamicStackHandler stacks = slotInventory.getStacks();
            for (int slot = 0; slot < stacks.getSlots(); slot++) {
                ItemStack stack = stacks.getStackInSlot(slot);
                if (AccessoryRestrictions.shouldDropEquipped(player, stack)) {
                    AccessoryRestrictions.drop(player, stack);
                    stacks.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
            curiosInventory.clearSlotModifiers();
        }));
    }
}
