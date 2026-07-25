package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;

public final class PlayerProgressClientState {
    private static volatile PlayerProgress progress = createProgress();

    private PlayerProgressClientState() {
    }

    public static Optional<PlayerProgress> get() {
        return Optional.ofNullable(progress);
    }

    public static boolean isSkillEnabled(Skill skill) {
        if (skill == null) {
            return false;
        }

        return get()
                .map(clientProgress -> skill.isEnabled(
                        clientProgress.getAptitudeLevel(skill.aptitude), clientProgress.getToggleSkill(skill)))
                .orElse(false);
    }

    public static int passiveLevel(Passive passive) {
        if (passive == null) {
            return 0;
        }

        return get().map(clientProgress -> clientProgress.getPassiveLevel(passive)).orElse(0);
    }

    public static boolean canToggleSkill(Skill skill) {
        if (skill == null) {
            return false;
        }

        return get()
                .map(clientProgress -> skill.canToggleAtLevel(clientProgress.getAptitudeLevel(skill.aptitude)))
                .orElse(false);
    }

    public static void applySync(CompoundTag tag) {
        if (tag == null) {
            return;
        }

        PlayerProgress syncedProgress = createProgress();
        syncedProgress.deserializeNBT(tag);
        progress = syncedProgress;
    }

    private static PlayerProgress createProgress() {
        return PlayerProgress.withNames(
                RegistryPassives.values().stream().map(passive -> passive.getName()).toList(),
                RegistrySkills.values().stream().map(skill -> skill.getName()).toList(),
                RegistryTitles.defaults());
    }
}
