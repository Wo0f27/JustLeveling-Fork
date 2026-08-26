package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.state.BlockState;

public final class MiningSkillEffects {
    private MiningSkillEffects() {
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
