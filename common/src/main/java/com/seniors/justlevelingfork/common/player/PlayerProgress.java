package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import net.minecraft.nbt.CompoundTag;

public class PlayerProgress {
    private static final int MAX_PERSISTED_APTITUDE_LEVEL = 1000;
    private final Collection<String> passiveNames;
    private final Collection<String> skillNames;
    private final Collection<Title> titles;

    public final Map<String, Integer> aptitudeLevel = new HashMap<>();
    public final Map<String, Integer> passiveLevel = new HashMap<>();
    public final Map<String, Boolean> toggleSkill = new HashMap<>();
    public final Map<String, Boolean> unlockTitle = new HashMap<>();
    public String playerTitle = RegistryTitles.TITLELESS.getName();
    public double betterCombatEntityRange = 0.0D;
    public int counterAttackTimer = 0;
    public float counterAttackDamage = 0.0F;
    public boolean counterAttack = false;

    public PlayerProgress(Collection<Passive> passives, Collection<Skill> skills, Collection<Title> titles) {
        this(passives.stream().map(Passive::getName).toList(), skills.stream().map(Skill::getName).toList(), titles);
    }

    private PlayerProgress(List<String> passiveNames, List<String> skillNames, Collection<Title> titles) {
        this.passiveNames = passiveNames;
        this.skillNames = skillNames;
        this.titles = titles;
        resetMaps();
    }

    public static PlayerProgress withDefaults(
            Collection<Passive> passives, Collection<Skill> skills, Collection<Title> titles) {
        return new PlayerProgress(passives, skills, titles);
    }

    public static PlayerProgress withNames(
            Collection<String> passiveNames, Collection<String> skillNames, Collection<Title> titles) {
        return new PlayerProgress(List.copyOf(passiveNames), List.copyOf(skillNames), titles);
    }

    public int getAptitudeLevel(Aptitude aptitude) {
        return getAptitudeLevel(aptitude.getName());
    }

    public int getAptitudeLevel(String aptitudeName) {
        return aptitudeLevel.getOrDefault(aptitudeName, 1);
    }

    public void setAptitudeLevel(Aptitude aptitude, int level) {
        setAptitudeLevel(aptitude.getName(), level);
    }

    public void setAptitudeLevel(String aptitudeName, int level) {
        aptitudeLevel.put(aptitudeName, Math.max(1, level));
    }

    public void addAptitudeLevel(Aptitude aptitude, int amount, int maxLevel) {
        setAptitudeLevel(aptitude, Math.min(getAptitudeLevel(aptitude) + amount, maxLevel));
    }

    public int getGlobalLevel() {
        return aptitudeLevel.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getSpentAptitudeExperience(IntUnaryOperator requiredPoints) {
        int spentExperience = 0;
        for (int aptitudeLevel : aptitudeLevel.values()) {
            for (int level = 1; level < aptitudeLevel; level++) {
                spentExperience += requiredPoints.applyAsInt(level);
            }
        }
        return spentExperience;
    }

    public int getPassiveLevel(Passive passive) {
        return passiveLevel.getOrDefault(passive.getName(), 0);
    }

    public void addPassiveLevel(Passive passive, int amount) {
        passiveLevel.put(passive.getName(), Math.min(getPassiveLevel(passive) + amount, passive.getMaxLevel()));
    }

    public void subPassiveLevel(Passive passive, int amount) {
        passiveLevel.put(passive.getName(), Math.max(getPassiveLevel(passive) - amount, 0));
    }

    public boolean getToggleSkill(Skill skill) {
        return getToggleSkill(skill.getName());
    }

    public boolean getToggleSkill(String skillName) {
        return toggleSkill.getOrDefault(skillName, false);
    }

    public void setToggleSkill(Skill skill, boolean enabled) {
        setToggleSkill(skill.getName(), enabled);
    }

    public void setToggleSkill(String skillName, boolean enabled) {
        toggleSkill.put(skillName, enabled);
    }

    public boolean getLockTitle(Title title) {
        return unlockTitle.getOrDefault(title.getName(), title.Requirement);
    }

    public void setUnlockTitle(Title title, boolean requirement) {
        unlockTitle.put(title.getName(), requirement);
    }

    public void setPlayerTitle(Title title) {
        playerTitle = title.getName();
    }

    public void resetSkills() {
        resetMaps();
        counterAttack = false;
        counterAttackTimer = 0;
        counterAttackDamage = 0.0F;
        betterCombatEntityRange = 0.0D;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        RegistryAptitudes.values().forEach(aptitude -> tag.putInt("aptitude." + aptitude.getName(), getAptitudeLevel(aptitude)));
        passiveNames.forEach(passiveName -> tag.putInt("passive." + passiveName, passiveLevel.getOrDefault(passiveName, 0)));
        skillNames.forEach(skillName -> tag.putBoolean("skill." + skillName, toggleSkill.getOrDefault(skillName, false)));
        RegistryTitles.defaults().forEach(title -> tag.putBoolean("title." + title.getName(), getLockTitle(title)));
        tag.putString("playerTitle", playerTitle);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        resetMaps();
        if (tag == null) {
            clearTransientState();
            return;
        }

        RegistryAptitudes.values().forEach(aptitude -> aptitudeLevel.put(
                aptitude.getName(),
                clamp(
                        readInt(tag, "aptitude." + aptitude.getName(), 1),
                        1,
                        Math.min(MAX_PERSISTED_APTITUDE_LEVEL, CommonConfigService.aptitudeMaxLevel()))));
        passiveNames.forEach(passiveName -> {
            Passive passive = RegistryPassives.getPassive(passiveName);
            int maximum = passive == null ? 0 : passive.getMaxLevel();
            passiveLevel.put(passiveName, clamp(readInt(tag, "passive." + passiveName, 0), 0, maximum));
        });
        skillNames.forEach(skillName -> toggleSkill.put(skillName, tag.getBoolean("skill." + skillName)));
        RegistryTitles.defaults().forEach(title -> {
            String key = "title." + title.getName();
            unlockTitle.put(title.getName(), tag.contains(key) ? tag.getBoolean(key) : title.Requirement);
        });

        playerTitle = validPlayerTitle(tag.contains("playerTitle") ? tag.getString("playerTitle") : null);
        clearTransientState();
    }

    public void copyFrom(PlayerProgress source) {
        if (source == this) {
            clearTransientState();
            return;
        }

        aptitudeLevel.clear();
        aptitudeLevel.putAll(source.aptitudeLevel);
        passiveLevel.clear();
        passiveLevel.putAll(source.passiveLevel);
        toggleSkill.clear();
        toggleSkill.putAll(source.toggleSkill);
        unlockTitle.clear();
        unlockTitle.putAll(source.unlockTitle);
        playerTitle = validPlayerTitle(source.playerTitle);
        clearTransientState();
    }

    private void resetMaps() {
        aptitudeLevel.clear();
        RegistryAptitudes.values().forEach(aptitude -> aptitudeLevel.put(aptitude.getName(), 1));
        passiveLevel.clear();
        passiveNames.forEach(passiveName -> passiveLevel.put(passiveName, 0));
        toggleSkill.clear();
        skillNames.forEach(skillName -> toggleSkill.put(skillName, false));
        unlockTitle.clear();
        RegistryTitles.defaults().forEach(title -> unlockTitle.put(title.getName(), title.Requirement));
    }

    private static int readInt(CompoundTag tag, String key, int defaultValue) {
        return tag.contains(key) ? tag.getInt(key) : defaultValue;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(value, Math.max(minimum, maximum)));
    }

    private String validPlayerTitle(String titleName) {
        Title title = RegistryTitles.getTitle(titleName);
        return title != null && getLockTitle(title) ? title.getName() : RegistryTitles.TITLELESS.getName();
    }

    private void clearTransientState() {
        counterAttackTimer = 0;
        counterAttackDamage = 0.0F;
        counterAttack = false;
        betterCombatEntityRange = 0.0D;
    }
}
