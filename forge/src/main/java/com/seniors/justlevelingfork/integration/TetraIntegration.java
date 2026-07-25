package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.util.TierHelper;

public final class TetraIntegration {
    private static final String PREFIX = "tetra*tier*";
    public static final List<String> TETRA_ITEMS = List.of(
            "tetra:modular_double",
            "tetra:modular_single",
            "tetra:modular_sword",
            "tetra:modular_shield",
            "tetra:modular_bow",
            "tetra:modular_crossbow");

    private TetraIntegration() {
    }

    public static List<String> getItemTypes(ItemStack item) {
        List<String> list = new ArrayList<>();
        if (!(item.getItem() instanceof ItemModularHandheld modularHandheld)) {
            return list;
        }

        Set<ToolAction> toolActions = modularHandheld.getToolActions(item);
        for (ToolAction action : toolActions) {
            String actionName = action.name();
            Tier tier = TierHelper.getTier(modularHandheld.getHarvestTier(item, action));
            if (tier == null) {
                Constants.LOG.warn("Item {} with action {} has a null Tetra tier.", item.getDisplayName().getString(), actionName);
                continue;
            }

            switch (actionName) {
                case "axe_wax_off", "axe_scrape", "axe_dig", "axe_strip" ->
                        list.add(String.format("%saxe:%s", PREFIX, tier.toString().toLowerCase()));
                case "pickaxe_dig" -> list.add(String.format("%spickaxe:%s", PREFIX, tier.toString().toLowerCase()));
                case "shovel_dig" -> list.add(String.format("%sshovel:%s", PREFIX, tier.toString().toLowerCase()));
                case "hoe_dig" -> list.add(String.format("%shoe:%s", PREFIX, tier.toString().toLowerCase()));
                case "cut" -> modularHandheld.getAllModules(item).stream()
                        .filter(module -> module.getKey().startsWith("sword/"))
                        .forEach(module -> {
                            VariantData variantData = module.getVariantData(item);
                            if (variantData != null && !Objects.equals(variantData.category, "misc")) {
                                String material = variantData.key.split("/")[1];
                                list.add(String.format("%ssword:%s", PREFIX, material.toLowerCase()));
                            }
                        });
                default -> {
                }
            }
        }

        return list;
    }
}
