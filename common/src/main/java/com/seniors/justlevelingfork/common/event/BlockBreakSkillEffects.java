package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTags;
import com.seniors.justlevelingfork.registry.skills.TreasureHunterSkill;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockBreakSkillEffects {
    private BlockBreakSkillEffects() {
    }

    public static void afterBlockBreak(Level level, ServerPlayer player, BlockPos pos, BlockState state) {
        if (level == null
                || level.isClientSide()
                || player == null
                || !state.is(RegistryTags.Blocks.DIRT)
                || !PlayerProgressService.isSkillEnabled(player, RegistrySkills.TREASURE_HUNTER)) {
            return;
        }

        ItemStack stack = TreasureHunterSkill.drop();
        if (!stack.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }
}
