package com.seniors.justlevelingfork.config.conditions;

import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;

public class BlockMinedCondition extends RegistryStatCondition<Block> {
    public BlockMinedCondition() {
        super("BlockMined", Stats.BLOCK_MINED, "Block");
    }
}
