package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.client.core.Aptitudes;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ClientLockItemRestrictions {
    private ClientLockItemRestrictions() {
    }

    public static boolean canUseItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Aptitudes> requirements = itemId == null ? null : HandlerAptitude.getValue(itemId.toString());
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        PlayerProgress progress = PlayerProgressClientState.get().orElse(null);
        if (progress == null) {
            return true;
        }

        return requirements.stream()
                .allMatch(requirement -> progress.getAptitudeLevel(requirement.getKey()) >= requirement.getAptitudeLvl());
    }
}
