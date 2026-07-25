package com.seniors.justlevelingfork.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class FabricRegistryAttributes {
    private FabricRegistryAttributes() {
    }

    public static void load() {
        RegistryAttributes.values().forEach(attribute ->
                Registry.register(BuiltInRegistries.ATTRIBUTE, RegistryAttributes.getId(attribute), attribute));
        addEntityAttributes();
    }

    private static void addEntityAttributes() {
        AttributeSupplier.Builder attributes = Player.createAttributes();
        RegistryAttributes.values().forEach(attributes::add);
        FabricDefaultAttributeRegistry.register(EntityType.PLAYER, attributes);
    }
}
