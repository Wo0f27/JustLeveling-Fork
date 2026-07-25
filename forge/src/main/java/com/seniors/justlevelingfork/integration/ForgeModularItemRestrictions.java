package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class ForgeModularItemRestrictions {
    private ForgeModularItemRestrictions() {
    }

    public static boolean canUse(ServerPlayer player, ItemStack stack) {
        if (player == null || player.isCreative() || stack.isEmpty()) {
            return true;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null && ModList.get().isLoaded("tetra") && TetraIntegration.TETRA_ITEMS.contains(itemId.toString())) {
            for (String restrictionId : TetraIntegration.getItemTypes(stack)) {
                if (!HandlerAptitude.canUseSpecificId(player, restrictionId)) {
                    return false;
                }
            }
        }

        if (MiapiIntegration.isModularItem(stack)) {
            for (ResourceLocation moduleId : MiapiIntegration.getModuleIds(stack)) {
                if (!HandlerAptitude.canUseSpecificId(player, moduleId.toString())) {
                    return false;
                }
            }
        }

        return true;
    }
}
