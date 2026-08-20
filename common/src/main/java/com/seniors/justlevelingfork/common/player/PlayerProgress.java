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
import net.minecraft.resources.ResourceLocation;

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
    // Character progression
    public long characterXp = 0L;
    public int pendingLevelUps = 0;
    public int pendingAdvancements = 0;
    // Multiclass progression.
    // Keys are namespaced class IDs, e.g. "justlevelingfork:fighter".
    public final Map<String, Integer> classLevels = new HashMap<>();
    // The class chosen when this character was first created.
    //
    // This is intentionally stored separately from classLevels because
    // multiclass proficiency rules may depend on which class came first.
    public String startingClass = "";
    // One subclass per class, e.g.
    // "justlevelingfork:fighter" -> "justlevelingfork:battle_master"
    public final Map<String, String> subclasses = new HashMap<>();
    // Feat ID -> rank. A map allows repeatable feats such as ASI.
    public final Map<String, Integer> feats = new HashMap<>();
    public double betterCombatEntityRange = 0.0D;
    public int counterAttackTimer = 0;
    public float counterAttackDamage = 0.0F;
    public boolean counterAttack = false;

    public boolean startingAbilitiesAssigned = false;

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

    public boolean isStartingAbilitiesAssigned() {return startingAbilitiesAssigned;}

    public void setStartingAbilitiesAssigned(boolean startingAbilitiesAssigned) {this.startingAbilitiesAssigned = startingAbilitiesAssigned;}

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

    public long getCharacterXp() {
        return characterXp;
    }

    public void setCharacterXp(long characterXp) {
        this.characterXp = Math.max(0L, characterXp);
    }

    public int getPendingLevelUps() {
        return pendingLevelUps;
    }

    public void setPendingLevelUps(int pendingLevelUps) {
        this.pendingLevelUps = Math.max(0, pendingLevelUps);
    }

    public int getPendingAdvancements() {
        return pendingAdvancements;
    }

    public void setPendingAdvancements(int pendingAdvancements) {
        this.pendingAdvancements = Math.max(0, pendingAdvancements);
    }

    public int getCharacterLevel() {
        return classLevels.values().stream()
                .mapToInt(level -> Math.max(0, level))
                .sum();
    }

    public int getClassLevel(String classId) {
        if (classId == null || classId.isBlank()) {
            return 0;
        }

        return Math.max(0, classLevels.getOrDefault(classId, 0));
    }

    public String getStartingClass() {
        return startingClass == null
                ? ""
                : startingClass;
    }

    public void setStartingClass(String classId) {

        if (classId == null || classId.isBlank()) {
            startingClass = "";
            return;
        }

        ResourceLocation id =
                ResourceLocation.tryParse(classId);

        startingClass =
                id == null
                        ? ""
                        : id.toString();
    }

    public boolean hasClass(String classId) {
        return getClassLevel(classId) > 0;
    }

    public void setClassLevel(String classId, int level) {
        if (classId == null || classId.isBlank()) {
            return;
        }

        if (level <= 0) {
            classLevels.remove(classId);
            subclasses.remove(classId);
            return;
        }

        classLevels.put(classId, level);
    }

    public String getSubclass(String classId) {
        if (classId == null || classId.isBlank()) {
            return "";
        }

        return subclasses.getOrDefault(classId, "");
    }

    public void setSubclass(String classId, String subclassId) {
        if (classId == null || classId.isBlank()) {
            return;
        }

        if (subclassId == null || subclassId.isBlank()) {
            subclasses.remove(classId);
            return;
        }

        subclasses.put(classId, subclassId);
    }

    public int getFeatRank(String featId) {
        if (featId == null || featId.isBlank()) {
            return 0;
        }

        return Math.max(0, feats.getOrDefault(featId, 0));
    }

    public void setFeatRank(String featId, int rank) {
        if (featId == null || featId.isBlank()) {
            return;
        }

        if (rank <= 0) {
            feats.remove(featId);
            return;
        }

        feats.put(featId, rank);
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
        tag.putLong("characterXp", characterXp);
        tag.putInt("pendingLevelUps", pendingLevelUps);
        tag.putInt("pendingAdvancements", pendingAdvancements);
        tag.putBoolean("startingAbilitiesAssigned", startingAbilitiesAssigned);
        tag.putString("startingClass", getStartingClass());

        CompoundTag classLevelsTag = new CompoundTag();
        classLevels.forEach(classLevelsTag::putInt);
        tag.put("classLevels", classLevelsTag);

        CompoundTag subclassesTag = new CompoundTag();
        subclasses.forEach(subclassesTag::putString);
        tag.put("subclasses", subclassesTag);

        CompoundTag featsTag = new CompoundTag();
        feats.forEach(featsTag::putInt);
        tag.put("feats", featsTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        resetMaps();
        resetCharacterProgression();
        if (tag == null) {
            clearTransientState();
            return;
        }

        characterXp = Math.max(0L, tag.getLong("characterXp"));
        pendingLevelUps = Math.max(0, tag.getInt("pendingLevelUps"));
        pendingAdvancements = Math.max(0, tag.getInt("pendingAdvancements"));

        startingAbilitiesAssigned = tag.getBoolean("startingAbilitiesAssigned");

        CompoundTag classLevelsTag = tag.getCompound("classLevels");
        classLevelsTag.getAllKeys().forEach(classId -> {
            int level = Math.max(0, classLevelsTag.getInt(classId));
            if (level > 0) {
                classLevels.put(classId, level);
            }
        });

        /*
         * Starting class was introduced after classLevels.
         *
         * New saves persist it explicitly.
         */
        startingClass = "";

        if (tag.contains("startingClass")) {

            ResourceLocation storedStartingClass =
                    ResourceLocation.tryParse(
                            tag.getString("startingClass"));

            if (storedStartingClass != null) {
                startingClass =
                        storedStartingClass.toString();
            }
        }

        /*
         * Backward compatibility:
         *
         * If an old save has exactly one class, it is safe to infer
         * that this was the starting class.
         *
         * If an old save is already multiclassed, we deliberately
         * do NOT guess based on HashMap iteration order.
         */
        if (startingClass.isBlank()
                && classLevels.size() == 1) {

            String onlyClass =
                    classLevels.keySet()
                            .iterator()
                            .next();

            ResourceLocation inferred =
                    ResourceLocation.tryParse(
                            onlyClass);

            if (inferred != null) {
                startingClass = inferred.toString();
            }
        }

        CompoundTag subclassesTag = tag.getCompound("subclasses");
        subclassesTag.getAllKeys().forEach(classId -> {
            String subclassId = subclassesTag.getString(classId);
            if (!subclassId.isBlank()) {
                subclasses.put(classId, subclassId);
            }
        });

        CompoundTag featsTag = tag.getCompound("feats");
        featsTag.getAllKeys().forEach(featId -> {
            int rank = Math.max(0, featsTag.getInt(featId));
            if (rank > 0) {
                feats.put(featId, rank);
            }
        });

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
        startingAbilitiesAssigned = source.startingAbilitiesAssigned;

        aptitudeLevel.clear();
        aptitudeLevel.putAll(source.aptitudeLevel);
        passiveLevel.clear();
        passiveLevel.putAll(source.passiveLevel);
        toggleSkill.clear();
        toggleSkill.putAll(source.toggleSkill);
        unlockTitle.clear();
        unlockTitle.putAll(source.unlockTitle);
        playerTitle = validPlayerTitle(source.playerTitle);
        characterXp = source.characterXp;
        pendingLevelUps = source.pendingLevelUps;
        pendingAdvancements = source.pendingAdvancements;
        startingClass = source.getStartingClass();

        classLevels.clear();
        classLevels.putAll(source.classLevels);

        subclasses.clear();
        subclasses.putAll(source.subclasses);

        feats.clear();
        feats.putAll(source.feats);
        clearTransientState();
    }

    public void resetCharacterProgression() {
        characterXp = 0L;
        pendingLevelUps = 0;
        pendingAdvancements = 0;
        startingAbilitiesAssigned = false;
        startingClass = "";

        classLevels.clear();
        subclasses.clear();
        feats.clear();
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
