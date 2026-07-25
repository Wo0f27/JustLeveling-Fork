package com.seniors.justlevelingfork.registry;

import static com.seniors.justlevelingfork.common.config.PassiveConfigService.levels10;
import static com.seniors.justlevelingfork.common.config.PassiveConfigService.levels5;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class RegistryPassives {
    public static final ResourceKey<Registry<Passive>> PASSIVES_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, "passives"));

    public static final Passive ATTACK_DAMAGE = register(
            "attack_damage",
            RegistryAptitudes.STRENGTH,
            "textures/skill/strength/passive_attack_damage.png",
            Attributes.ATTACK_DAMAGE,
            "96a891fe-5919-418d-8205-f50464391500",
            1.5D,
            levels10());
    public static final Passive ATTACK_KNOCKBACK = register(
            "attack_knockback",
            RegistryAptitudes.STRENGTH,
            "textures/skill/strength/passive_attack_knockback.png",
            Attributes.ATTACK_KNOCKBACK,
            "96a891fe-5919-418d-8205-f50464391501",
            0.4D,
            levels5());
    public static final Passive MAX_HEALTH = register(
            "max_health",
            RegistryAptitudes.CONSTITUTION,
            "textures/skill/constitution/passive_max_health.png",
            Attributes.MAX_HEALTH,
            "96a891fe-5919-418d-8205-f50464391502",
            20.0D,
            levels10());
    public static final Passive KNOCKBACK_RESISTANCE = register(
            "knockback_resistance",
            RegistryAptitudes.CONSTITUTION,
            "textures/skill/constitution/passive_knockback_resistance.png",
            Attributes.KNOCKBACK_RESISTANCE,
            "96a891fe-5919-418d-8205-f50464391503",
            0.5D,
            levels5());
    public static final Passive MOVEMENT_SPEED = register(
            "movement_speed",
            RegistryAptitudes.DEXTERITY,
            "textures/skill/dexterity/passive_movement_speed.png",
            Attributes.MOVEMENT_SPEED,
            "96a891fe-5919-418d-8205-f50464391504",
            0.05D,
            levels5());
    public static final Passive PROJECTILE_DAMAGE = register(
            "projectile_damage",
            RegistryAptitudes.DEXTERITY,
            "textures/skill/dexterity/passive_projectile_damage.png",
            RegistryAttributes.PROJECTILE_DAMAGE,
            "96a891fe-5919-418d-8205-f50464391505",
            5.0D,
            levels5());
    public static final Passive ARMOR = register(
            "armor",
            RegistryAptitudes.DEFENSE,
            "textures/skill/defense/passive_armor.png",
            Attributes.ARMOR,
            "96a891fe-5919-418d-8205-f50464391506",
            4.0D,
            levels5());
    public static final Passive ARMOR_TOUGHNESS = register(
            "armor_toughness",
            RegistryAptitudes.DEFENSE,
            "textures/skill/defense/passive_armor_toughness.png",
            Attributes.ARMOR_TOUGHNESS,
            "96a891fe-5919-418d-8205-f50464391507",
            1.0D,
            levels5());
    public static final Passive ATTACK_SPEED = register(
            "attack_speed",
            RegistryAptitudes.INTELLIGENCE,
            "textures/skill/intelligence/passive_attack_speed.png",
            Attributes.ATTACK_SPEED,
            "96a891fe-5919-418d-8205-f50464391508",
            0.4D,
            levels5());
    public static final Passive ENTITY_REACH = register(
            "entity_reach",
            RegistryAptitudes.INTELLIGENCE,
            "textures/skill/intelligence/passive_entity_reach.png",
            RegistryAttributes.ENTITY_REACH,
            "96a891fe-5919-418d-8205-f50464391509",
            1.0D,
            levels5());
    public static final Passive BLOCK_REACH = register(
            "block_reach",
            RegistryAptitudes.BUILDING,
            "textures/skill/building/passive_block_reach.png",
            RegistryAttributes.BLOCK_REACH,
            "96a891fe-5919-418d-8205-f50464391510",
            1.5D,
            levels5());
    public static final Passive BREAK_SPEED = register(
            "break_speed",
            RegistryAptitudes.BUILDING,
            "textures/skill/building/passive_break_speed.png",
            RegistryAttributes.BREAK_SPEED,
            "96a891fe-5919-418d-8205-f50464391511",
            0.5D,
            levels5());
    public static final Passive BENEFICIAL_EFFECT = register(
            "beneficial_effect",
            RegistryAptitudes.MAGIC,
            "textures/skill/magic/passive_beneficial_effect.png",
            RegistryAttributes.BENEFICIAL_EFFECT,
            "96a891fe-5919-418d-8205-f50464391512",
            60.0D,
            levels10());
    public static final Passive MAGIC_RESIST = register(
            "magic_resist",
            RegistryAptitudes.MAGIC,
            "textures/skill/magic/passive_magic_resist.png",
            RegistryAttributes.MAGIC_RESIST,
            "96a891fe-5919-418d-8205-f50464391513",
            0.5D,
            levels5());
    public static final Passive CRITICAL_DAMAGE = register(
            "critical_damage",
            RegistryAptitudes.LUCK,
            "textures/skill/luck/passive_critical_damage.png",
            RegistryAttributes.CRITICAL_DAMAGE,
            "96a891fe-5919-418d-8205-f50464391515",
            0.25D,
            levels10());
    public static final Passive LUCK = register(
            "luck",
            RegistryAptitudes.LUCK,
            "textures/skill/luck/passive_luck.png",
            Attributes.LUCK,
            "96a891fe-5919-418d-8205-f50464391514",
            2.0D,
            levels10());

    private static final Map<String, Passive> PASSIVES_BY_NAME = new LinkedHashMap<>();

    private RegistryPassives() {
    }

    public static Collection<Passive> values() {
        ensureLoaded();
        return PASSIVES_BY_NAME.values();
    }

    public static Passive getPassive(String passiveName) {
        if (passiveName == null || passiveName.isBlank()) {
            return null;
        }

        ensureLoaded();
        return PASSIVES_BY_NAME.get(path(passiveName));
    }

    private static Passive register(
            String name,
            Aptitude aptitude,
            String texture,
            Attribute attribute,
            String attributeUuid,
            double attributeValue,
            int... levelsRequired) {
        ResourceLocation key = new ResourceLocation(Constants.MOD_ID, name);
        return new Passive(
                key, aptitude, HandlerResources.create(texture), attribute, attributeUuid, attributeValue, levelsRequired);
    }

    private static void ensureLoaded() {
        if (!PASSIVES_BY_NAME.isEmpty()) {
            return;
        }

        put(ATTACK_DAMAGE);
        put(ATTACK_KNOCKBACK);
        put(MAX_HEALTH);
        put(KNOCKBACK_RESISTANCE);
        put(MOVEMENT_SPEED);
        put(PROJECTILE_DAMAGE);
        put(ARMOR);
        put(ARMOR_TOUGHNESS);
        put(ATTACK_SPEED);
        put(ENTITY_REACH);
        put(BLOCK_REACH);
        put(BREAK_SPEED);
        put(BENEFICIAL_EFFECT);
        put(MAGIC_RESIST);
        put(CRITICAL_DAMAGE);
        put(LUCK);
    }

    private static void put(Passive passive) {
        PASSIVES_BY_NAME.put(passive.getName(), passive);
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
