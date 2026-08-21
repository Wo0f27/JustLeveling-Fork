package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.player.CharacterClassDefinition;
import com.seniors.justlevelingfork.common.player.ClassAbilityRequirement;
import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RegistryClasses {

    /*
     * Canonical class IDs.
     *
     * Keep these stable. PlayerProgress stores class levels
     * using these namespaced IDs.
     */
    public static final ResourceLocation BARBARIAN = id("barbarian");
    public static final ResourceLocation BARD = id("bard");
    public static final ResourceLocation CLERIC = id("cleric");
    public static final ResourceLocation DRUID = id("druid");
    public static final ResourceLocation FIGHTER = id("fighter");
    public static final ResourceLocation MONK = id("monk");
    public static final ResourceLocation PALADIN = id("paladin");
    public static final ResourceLocation RANGER = id("ranger");
    public static final ResourceLocation ROGUE = id("rogue");
    public static final ResourceLocation SORCERER = id("sorcerer");
    public static final ResourceLocation WARLOCK = id("warlock");
    public static final ResourceLocation WIZARD = id("wizard");

    private static final List<ResourceLocation> VALUES =
            List.of(
                    BARBARIAN,
                    BARD,
                    CLERIC,
                    DRUID,
                    FIGHTER,
                    MONK,
                    PALADIN,
                    RANGER,
                    ROGUE,
                    SORCERER,
                    WARLOCK,
                    WIZARD
            );

    private static final Map<ResourceLocation, CharacterClassDefinition>
            DEFINITIONS = createDefinitions();

    private RegistryClasses() {
    }

    public static List<ResourceLocation> values() {
        return VALUES;
    }

    public static ResourceLocation get(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        ResourceLocation id =
                ResourceLocation.tryParse(
                        value.contains(":")
                                ? value
                                : Constants.MOD_ID + ":" + value);

        return id != null && VALUES.contains(id)
                ? id
                : null;
    }

    public static CharacterClassDefinition getDefinition(
            ResourceLocation classId) {

        if (classId == null) {
            return null;
        }

        return DEFINITIONS.get(classId);
    }

    public static CharacterClassDefinition getDefinition(
            String classId) {

        ResourceLocation id = get(classId);

        return id == null
                ? null
                : getDefinition(id);
    }

    public static List<CharacterClassDefinition> definitions() {
        return VALUES.stream()
                .map(DEFINITIONS::get)
                .toList();
    }

    private static Map<ResourceLocation, CharacterClassDefinition>
    createDefinitions() {

        Map<ResourceLocation, CharacterClassDefinition> result =
                new LinkedHashMap<>();

        /*
         * BARBARIAN
         *
         * Starting:
         * Light, Medium, Shields
         *
         * Multiclass:
         * Shields only
         */
        result.put(
                BARBARIAN,
                definition(
                        BARBARIAN,
                        scores(15, 13, 14, 10, 12, 11),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.STRENGTH),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                        armor(
                                ArmorCategory.SHIELD),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE,
                        RegistryWeaponProficiencies.MARTIAL),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE,
                        RegistryWeaponProficiencies.MARTIAL)));


        /*
         * BARD
         */
        result.put(
                BARD,
                definition(
                        BARD,
                        scores(10, 14, 13, 11, 12, 15),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.CHARISMA),
                        armor(
                                ArmorCategory.LIGHT),
                        armor(
                                ArmorCategory.LIGHT),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE,
                        RegistryWeaponProficiencies.HAND_CROSSBOW,
                        RegistryWeaponProficiencies.LONGSWORD,
                        RegistryWeaponProficiencies.RAPIER,
                        RegistryWeaponProficiencies.SHORTSWORD),
                weapons()));

        /*
         * CLERIC
         */
        result.put(
                CLERIC,
                definition(
                        CLERIC,
                        scores(13, 12, 14, 10, 15, 11),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.WISDOM),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE),
                weapons()));

        /*
         * DRUID
         *
         * The 2014 restriction regarding metal armor is NOT
         * an armor-proficiency rule. That remains a separate
         * equipment/feature restriction.
         */
        result.put(
                DRUID,
                definition(
                        DRUID,
                        scores(11, 13, 14, 12, 15, 10),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.WISDOM),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                weapons(
                        RegistryWeaponProficiencies.CLUB,
                        RegistryWeaponProficiencies.DAGGER,
                        RegistryWeaponProficiencies.DART,
                        RegistryWeaponProficiencies.JAVELIN,
                        RegistryWeaponProficiencies.MACE,
                        RegistryWeaponProficiencies.QUARTERSTAFF,
                        RegistryWeaponProficiencies.SCIMITAR,
                        RegistryWeaponProficiencies.SICKLE,
                        RegistryWeaponProficiencies.SLING,
                        RegistryWeaponProficiencies.SPEAR),
                weapons()));

        /*
         * FIGHTER
         *
         * STR 13 OR DEX 13.
         *
         * Starting Fighter gets Heavy Armor.
         * Multiclassing into Fighter does not.
         */
        result.put(
                FIGHTER,
                definition(
                        FIGHTER,
                        scores(15, 13, 14, 10, 12, 11),
                        ClassAbilityRequirement.anyOf(
                                13,
                                RegistryAptitudes.STRENGTH,
                                RegistryAptitudes.DEXTERITY),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.HEAVY,
                                ArmorCategory.SHIELD),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE,
                        RegistryWeaponProficiencies.MARTIAL),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE,
                        RegistryWeaponProficiencies.MARTIAL)));

        /*
         * MONK
         */
        result.put(
                MONK,
                definition(
                        MONK,
                        scores(12, 15, 13, 11, 14, 10),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.DEXTERITY,
                                RegistryAptitudes.WISDOM),
                        armor(),
                        armor(),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE,
                        RegistryWeaponProficiencies.SHORTSWORD),
                weapons(
                        RegistryWeaponProficiencies.SIMPLE,
                        RegistryWeaponProficiencies.SHORTSWORD)));

        /*
         * PALADIN
         *
         * Starting Paladin gets Heavy Armor.
         * Multiclassing into Paladin does not.
         */
        result.put(
                PALADIN,
                definition(
                        PALADIN,
                        scores(15, 11, 13, 10, 12, 14),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.STRENGTH,
                                RegistryAptitudes.CHARISMA),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.HEAVY,
                                ArmorCategory.SHIELD),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                        weapons(
                                RegistryWeaponProficiencies.SIMPLE,
                                RegistryWeaponProficiencies.MARTIAL),
                        weapons(
                                RegistryWeaponProficiencies.SIMPLE,
                                RegistryWeaponProficiencies.MARTIAL)));

        /*
         * RANGER
         */
        result.put(
                RANGER,
                definition(
                        RANGER,
                        scores(12, 15, 13, 10, 14, 11),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.DEXTERITY,
                                RegistryAptitudes.WISDOM),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                        armor(
                                ArmorCategory.LIGHT,
                                ArmorCategory.MEDIUM,
                                ArmorCategory.SHIELD),
                        weapons(
                                RegistryWeaponProficiencies.SIMPLE,
                                RegistryWeaponProficiencies.MARTIAL),
                        weapons(
                                RegistryWeaponProficiencies.SIMPLE,
                                RegistryWeaponProficiencies.MARTIAL)));

        /*
         * ROGUE
         */
        result.put(
                ROGUE,
                definition(
                        ROGUE,
                        scores(10, 15, 14, 11, 13, 12),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.DEXTERITY),
                        armor(
                                ArmorCategory.LIGHT),
                        armor(
                                ArmorCategory.LIGHT),
                        weapons(
                                RegistryWeaponProficiencies.SIMPLE,
                                RegistryWeaponProficiencies.HAND_CROSSBOW,
                                RegistryWeaponProficiencies.LONGSWORD,
                                RegistryWeaponProficiencies.RAPIER,
                                RegistryWeaponProficiencies.SHORTSWORD),
                        weapons()));

        /*
         * SORCERER
         */
        result.put(
                SORCERER,
                definition(
                        SORCERER,
                        scores(10, 13, 14, 11, 12, 15),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.CHARISMA),
                        armor(),
                        armor(),
                        weapons(
                                RegistryWeaponProficiencies.DAGGER,
                                RegistryWeaponProficiencies.DART,
                                RegistryWeaponProficiencies.SLING,
                                RegistryWeaponProficiencies.QUARTERSTAFF,
                                RegistryWeaponProficiencies.LIGHT_CROSSBOW),
                        weapons()));

        /*
         * WARLOCK
         */
        result.put(
                WARLOCK,
                definition(
                        WARLOCK,
                        scores(10, 13, 14, 11, 12, 15),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.CHARISMA),
                        armor(
                                ArmorCategory.LIGHT),
                        armor(
                                ArmorCategory.LIGHT),
                        weapons(
                                RegistryWeaponProficiencies.SIMPLE),
                        weapons(
                                RegistryWeaponProficiencies.SIMPLE)));

        /*
         * WIZARD
         */
        result.put(
                WIZARD,
                definition(
                        WIZARD,
                        scores(10, 13, 14, 15, 12, 11),
                        ClassAbilityRequirement.allOf(
                                13,
                                RegistryAptitudes.INTELLIGENCE),
                        armor(),
                        armor(),
                        weapons(
                                RegistryWeaponProficiencies.DAGGER,
                                RegistryWeaponProficiencies.DART,
                                RegistryWeaponProficiencies.SLING,
                                RegistryWeaponProficiencies.QUARTERSTAFF,
                                RegistryWeaponProficiencies.LIGHT_CROSSBOW),
                        weapons()));

        return Map.copyOf(result);
    }

    private static CharacterClassDefinition definition(
            ResourceLocation id,
            Map<Aptitude, Integer> recommendedAbilityScores,
            ClassAbilityRequirement multiclassRequirement,
            Set<ArmorCategory> startingArmor,
            Set<ArmorCategory> multiclassArmor,
            Set<ResourceLocation> startingWeapons,
            Set<ResourceLocation> multiclassWeapons) {

        return new CharacterClassDefinition(
                id,
                recommendedAbilityScores,
                multiclassRequirement,
                startingArmor,
                multiclassArmor,
                startingWeapons,
                multiclassWeapons);
    }

    private static Map<Aptitude, Integer> scores(
            int strength,
            int dexterity,
            int constitution,
            int intelligence,
            int wisdom,
            int charisma) {

        Map<Aptitude, Integer> scores =
                new LinkedHashMap<>();

        scores.put(
                RegistryAptitudes.STRENGTH,
                strength);

        scores.put(
                RegistryAptitudes.DEXTERITY,
                dexterity);

        scores.put(
                RegistryAptitudes.CONSTITUTION,
                constitution);

        scores.put(
                RegistryAptitudes.INTELLIGENCE,
                intelligence);

        scores.put(
                RegistryAptitudes.WISDOM,
                wisdom);

        scores.put(
                RegistryAptitudes.CHARISMA,
                charisma);

        return scores;
    }

    private static Set<ArmorCategory> armor(
            ArmorCategory... categories) {

        if (categories == null || categories.length == 0) {
            return Set.of();
        }

        return Set.of(categories);
    }

    private static Set<ResourceLocation> weapons(
            ResourceLocation... proficiencies) {

        if (proficiencies == null
                || proficiencies.length == 0) {
            return Set.of();
        }

        return Set.of(proficiencies);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(
                Constants.MOD_ID,
                path);
    }
}