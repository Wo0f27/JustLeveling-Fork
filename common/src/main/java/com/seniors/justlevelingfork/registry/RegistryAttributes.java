package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.platform.ReachAttributeBridge;
import com.seniors.justlevelingfork.registry.passive.Passive;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;

public final class RegistryAttributes {
    public static final Attribute BREAK_SPEED = register("break_speed");
    public static final Attribute CRITICAL_DAMAGE = register("critical_damage");
    public static final Attribute PROJECTILE_DAMAGE = register("projectile_damage");
    public static final Attribute BENEFICIAL_EFFECT = register("beneficial_effect");
    public static final Attribute MAGIC_RESIST = register("magic_resist");
    public static final Attribute ENTITY_REACH = register("entity_reach");
    public static final Attribute BLOCK_REACH = register("block_reach");

    private static final Map<ResourceLocation, Attribute> ATTRIBUTES_BY_ID = new LinkedHashMap<>();

    private RegistryAttributes() {
    }

    public static Collection<Attribute> values() {
        ensureLoaded();
        return ATTRIBUTES_BY_ID.values();
    }

    public static ResourceLocation getId(Attribute attribute) {
        ensureLoaded();
        return ATTRIBUTES_BY_ID.entrySet().stream()
                .filter(entry -> entry.getValue() == attribute)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown attribute: " + attribute.getDescriptionId()));
    }

    public static Attribute getAttribute(String name) {
        ensureLoaded();
        return ATTRIBUTES_BY_ID.get(new ResourceLocation(Constants.MOD_ID, name.toLowerCase(Locale.ROOT)));
    }

    public static double passiveModifierAmount(Passive passive, int passiveLevel) {
        int maxLevel = passive.getMaxLevel();
        if (maxLevel <= 0 || passiveLevel <= 0) {
            return 0.0D;
        }
        return passive.getValue() / maxLevel * Math.min(passiveLevel, maxLevel);
    }

    public static void applyPassiveModifier(Player player, Passive passive, int passiveLevel) {
        applyPermanentAddition(
                player,
                passive.attribute,
                passiveModifierAmount(passive, passiveLevel),
                UUID.fromString(passive.attributeUuid),
                passiveLevel > 0);
        ReachAttributeBridge.apply(player, passive, passiveLevel);
    }

    public static void applyPermanentAddition(Player player, Attribute attribute, double amount, UUID uuid, boolean enabled) {
        if (player == null || attribute == null || uuid == null) {
            return;
        }

        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier oldModifier = instance.getModifier(uuid);
        if (oldModifier != null) {
            instance.removeModifier(oldModifier);
        }

        if (enabled) {
            instance.addPermanentModifier(new AttributeModifier(
                    uuid, Constants.MOD_ID, amount, AttributeModifier.Operation.ADDITION));
        }
    }

    private static Attribute register(String name) {
        return new RangedAttribute("attribute.name." + Constants.MOD_ID + "." + name, 0.0D, 0.0D, 1024.0D)
                .setSyncable(true);
    }

    private static void ensureLoaded() {
        if (!ATTRIBUTES_BY_ID.isEmpty()) {
            return;
        }

        put("break_speed", BREAK_SPEED);
        put("critical_damage", CRITICAL_DAMAGE);
        put("projectile_damage", PROJECTILE_DAMAGE);
        put("beneficial_effect", BENEFICIAL_EFFECT);
        put("magic_resist", MAGIC_RESIST);
        put("entity_reach", ENTITY_REACH);
        put("block_reach", BLOCK_REACH);
    }

    private static void put(String name, Attribute attribute) {
        ATTRIBUTES_BY_ID.put(new ResourceLocation(Constants.MOD_ID, name), attribute);
    }
}
