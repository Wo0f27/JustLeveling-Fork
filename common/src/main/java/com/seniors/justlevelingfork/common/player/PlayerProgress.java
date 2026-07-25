package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
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
        tag.putInt("counterAttackTimer", counterAttackTimer);
        tag.putFloat("counterAttackDamage", counterAttackDamage);
        tag.putBoolean("counterAttack", counterAttack);
        tag.putString("playerTitle", playerTitle);
        tag.putDouble("betterCombatEntityRange", betterCombatEntityRange);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        RegistryAptitudes.values().forEach(aptitude ->
                aptitudeLevel.put(aptitude.getName(), readInt(tag, "aptitude." + aptitude.getName(), 1)));
        passiveNames.forEach(passiveName ->
                passiveLevel.put(passiveName, readInt(tag, "passive." + passiveName, 0)));
        skillNames.forEach(skillName -> toggleSkill.put(skillName, tag.getBoolean("skill." + skillName)));
        RegistryTitles.defaults().forEach(title -> unlockTitle.put(title.getName(), tag.getBoolean("title." + title.getName())));

        counterAttackTimer = tag.getInt("counterAttackTimer");
        counterAttackDamage = tag.getFloat("counterAttackDamage");
        counterAttack = tag.getBoolean("counterAttack");
        playerTitle = tag.contains("playerTitle") ? tag.getString("playerTitle") : RegistryTitles.TITLELESS.getName();
        betterCombatEntityRange = tag.getDouble("betterCombatEntityRange");
    }

    public void copyFrom(PlayerProgress source) {
        aptitudeLevel.clear();
        aptitudeLevel.putAll(source.aptitudeLevel);
        passiveLevel.clear();
        passiveLevel.putAll(source.passiveLevel);
        toggleSkill.clear();
        toggleSkill.putAll(source.toggleSkill);
        unlockTitle.clear();
        unlockTitle.putAll(source.unlockTitle);
        counterAttackTimer = source.counterAttackTimer;
        counterAttackDamage = source.counterAttackDamage;
        counterAttack = source.counterAttack;
        playerTitle = source.playerTitle;
        betterCombatEntityRange = source.betterCombatEntityRange;
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
}
