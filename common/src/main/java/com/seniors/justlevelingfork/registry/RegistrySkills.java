package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.client.core.ValueType;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.skills.Skill;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistrySkills {
    public static final ResourceKey<Registry<Skill>> SKILLS_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, "skills"));

    public static final Skill ONE_HANDED = register(
            "one_handed",
            RegistryAptitudes.STRENGTH,
            10,
            HandlerResources.ONE_HANDED_SKILL,
            new Value(ValueType.AMPLIFIER, 0.5F));
    public static final Skill FIGHTING_SPIRIT = register(
            "fighting_spirit",
            RegistryAptitudes.STRENGTH,
            16,
            HandlerResources.FIGHTING_SPIRIT_SKILL,
            new Value(ValueType.BOOST, 1),
            new Value(ValueType.DURATION, 3));
    public static final Skill BERSERKER = register(
            "berserker",
            RegistryAptitudes.STRENGTH,
            30,
            HandlerResources.BERSERKER_SKILL,
            new Value(ValueType.PERCENT, 30));
    public static final Skill ATHLETICS = register(
            "athletics",
            RegistryAptitudes.CONSTITUTION,
            10,
            HandlerResources.ATHLETICS_SKILL,
            new Value(ValueType.MODIFIER, 1.5F));
    public static final Skill TURTLE_SHIELD = register(
            "turtle_shield", RegistryAptitudes.CONSTITUTION, 20, HandlerResources.TURTLE_SHIELD_SKILL);
    public static final Skill LION_HEART = register(
            "lion_heart",
            RegistryAptitudes.CONSTITUTION,
            30,
            HandlerResources.LION_HEART_SKILL,
            new Value(ValueType.PERCENT, 50));
    public static final Skill QUICK_REPOSITION = register(
            "quick_reposition",
            RegistryAptitudes.DEXTERITY,
            10,
            HandlerResources.QUICK_REPOSITION_SKILL,
            new Value(ValueType.BOOST, 2),
            new Value(ValueType.DURATION, 3));
    public static final Skill STEALTH_MASTERY = register(
            "stealth_mastery",
            RegistryAptitudes.DEXTERITY,
            16,
            HandlerResources.STEALTH_MASTERY_SKILL,
            new Value(ValueType.PERCENT, 20),
            new Value(ValueType.PERCENT, 60),
            new Value(ValueType.MODIFIER, 1.25F));
    public static final Skill CAT_EYES =
            register("cat_eyes", RegistryAptitudes.DEXTERITY, 30, HandlerResources.CAT_EYES_SKILL);
    public static final Skill SNOW_WALKER =
            register("snow_walker", RegistryAptitudes.DEXTERITY, 10, HandlerResources.SNOW_WALKER_SKILL);
    public static final Skill COUNTER_ATTACK = register(
            "counter_attack",
            RegistryAptitudes.DEXTERITY,
            18,
            HandlerResources.COUNTER_ATTACK_SKILL,
            new Value(ValueType.DURATION, 3),
            new Value(ValueType.PERCENT, 50));
    public static final Skill DIAMOND_SKIN = register(
            "diamond_skin",
            RegistryAptitudes.DEXTERITY,
            30,
            HandlerResources.DIAMOND_SKIN_SKILL,
            new Value(ValueType.BOOST, 2),
            new Value(ValueType.AMPLIFIER, 2.0F));
    public static final Skill SCHOLAR =
            register("scholar", RegistryAptitudes.INTELLIGENCE, 8, HandlerResources.SCHOLAR_SKILL);
    public static final Skill HAGGLER = register(
            "haggler",
            RegistryAptitudes.INTELLIGENCE,
            16,
            HandlerResources.HAGGLER_SKILL,
            new Value(ValueType.PERCENT, 20));
    public static final Skill ALCHEMY_MANIPULATION = register(
            "alchemy_manipulation",
            RegistryAptitudes.INTELLIGENCE,
            30,
            HandlerResources.ALCHEMY_MANIPULATION_SKILL,
            new Value(ValueType.AMPLIFIER, 1.0F));
    public static final Skill OBSIDIAN_SMASHER = register(
            "obsidian_smasher",
            RegistryAptitudes.STRENGTH,
            12,
            HandlerResources.OBSIDIAN_SMASHER_SKILL,
            new Value(ValueType.MODIFIER, 10.0F));
    public static final Skill TREASURE_HUNTER = register(
            "treasure_hunter",
            RegistryAptitudes.WISDOM,
            20,
            HandlerResources.TREASURE_HUNTER_SKILL,
            new Value(ValueType.PROBABILITY, 500));
    public static final Skill CONVERGENCE = register(
            "convergence",
            RegistryAptitudes.INTELLIGENCE,
            30,
            HandlerResources.CONVERGENCE_SKILL,
            new Value(ValueType.PROBABILITY, 8));
    public static final Skill SAFE_PORT =
            register("safe_port", RegistryAptitudes.INTELLIGENCE, 12, HandlerResources.SAFE_PORT_SKILL);
    public static final Skill LIFE_EATER = register(
            "life_eater",
            RegistryAptitudes.CHARISMA,
            18,
            HandlerResources.LIFE_EATER_SKILL,
            new Value(ValueType.AMPLIFIER, 1.0F));
    public static final Skill WORMHOLE_STORAGE =
            register("wormhole_storage", RegistryAptitudes.INTELLIGENCE, 30, HandlerResources.WORMHOLE_STORAGE_SKILL);
    public static final Skill CRITICAL_ROLL = register(
            "critical_roll",
            RegistryAptitudes.DEXTERITY,
            12,
            HandlerResources.CRITICAL_ROLL_SKILL,
            new Value(ValueType.MODIFIER, 1.25F),
            new Value(ValueType.PROBABILITY, 3));
    public static final Skill LUCKY_DROP = register(
            "lucky_drop",
            RegistryAptitudes.WISDOM,
            22,
            HandlerResources.LUCKY_DROP_SKILL,
            new Value(ValueType.PROBABILITY, 10),
            new Value(ValueType.MODIFIER, 2.0F));
    public static final Skill LIMIT_BREAKER = register(
            "limit_breaker",
            RegistryAptitudes.CONSTITUTION,
            32,
            HandlerResources.LIMIT_BREAKER_SKILL,
            new Value(ValueType.PROBABILITY, 100),
            new Value(ValueType.AMPLIFIER, 999.0F));

    private static final Map<String, Skill> SKILLS_BY_NAME = new LinkedHashMap<>();

    private RegistrySkills() {
    }

    public static Collection<Skill> values() {
        ensureLoaded();
        return SKILLS_BY_NAME.values();
    }

    public static Skill getSkill(String skillName) {
        if (skillName == null || skillName.isBlank()) {
            return null;
        }

        ensureLoaded();
        return SKILLS_BY_NAME.get(path(skillName));
    }

    private static Skill register(
            String name, Aptitude aptitude, int requiredLevel, ResourceLocation texture, Value... configValues) {
        return new Skill(new ResourceLocation(Constants.MOD_ID, name), aptitude, requiredLevel, texture, configValues);
    }

    private static void ensureLoaded() {
        if (!SKILLS_BY_NAME.isEmpty()) {
            return;
        }

        put(ONE_HANDED);
        put(FIGHTING_SPIRIT);
        put(BERSERKER);
        put(ATHLETICS);
        put(TURTLE_SHIELD);
        put(LION_HEART);
        put(QUICK_REPOSITION);
        put(STEALTH_MASTERY);
        put(CAT_EYES);
        put(SNOW_WALKER);
        put(COUNTER_ATTACK);
        put(DIAMOND_SKIN);
        put(SCHOLAR);
        put(HAGGLER);
        put(ALCHEMY_MANIPULATION);
        put(OBSIDIAN_SMASHER);
        put(TREASURE_HUNTER);
        put(CONVERGENCE);
        put(SAFE_PORT);
        put(LIFE_EATER);
        put(WORMHOLE_STORAGE);
        put(CRITICAL_ROLL);
        put(LUCKY_DROP);
        put(LIMIT_BREAKER);
    }

    private static void put(Skill skill) {
        SKILLS_BY_NAME.put(skill.getName(), skill);
    }

    private static String path(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0) {
            return normalized.substring(namespaceSeparator + 1);
        }
        return normalized;
    }
}
