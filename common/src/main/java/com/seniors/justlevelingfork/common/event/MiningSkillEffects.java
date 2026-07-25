package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.state.BlockState;

public final class MiningSkillEffects {
    private MiningSkillEffects() {
    }

    public static float adjustDestroySpeed(Player player, BlockState state, float currentSpeed) {
        if (player == null || state == null) {
            return currentSpeed;
        }

        ItemStack mainHand = player.getMainHandItem();
        Item item = mainHand.getItem();
        if (!(item instanceof PickaxeItem) && !(item instanceof ShovelItem) && !(item instanceof AxeItem)) {
            return currentSpeed;
        }

        return currentSpeed * (1.0F + (float) player.getAttributeValue(RegistryAttributes.BREAK_SPEED));
    }

    public static float adjustObsidianDestroyProgress(Player player, BlockState state, float currentProgress) {
        if (player == null || state == null || !state.is(RegistryTags.Blocks.OBSIDIAN)) {
            return currentProgress;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof PickaxeItem) || mainHand.getDestroySpeed(state) <= 1.0F) {
            return currentProgress;
        }

        if (player instanceof ServerPlayer serverPlayer
                && PlayerProgressService.isSkillEnabled(serverPlayer, RegistrySkills.OBSIDIAN_SMASHER)) {
            return (float) (currentProgress * RegistrySkills.OBSIDIAN_SMASHER.getValue()[0]);
        }

        return currentProgress;
    }
}
