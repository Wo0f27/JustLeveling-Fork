package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.ForgeRegistryCapabilities;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ForgePlayerProgressProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerProgress progress =
            PlayerProgress.withNames(
                    RegistryPassives.values().stream().map(passive -> passive.getName()).toList(),
                    RegistrySkills.values().stream().map(skill -> skill.getName()).toList(),
                    RegistryTitles.defaults());
    private final LazyOptional<PlayerProgress> optional = LazyOptional.of(() -> progress);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ForgeRegistryCapabilities.PLAYER_PROGRESS ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return progress.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        progress.deserializeNBT(tag);
    }
}
