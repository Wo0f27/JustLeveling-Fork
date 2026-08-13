package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAttributes;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class ExternalAttributeService {

    private ExternalAttributeService() {
    }

    public static void applyPermanentAddition(
            Player player,
            String namespace,
            String path,
            double amount,
            UUID uuid,
            boolean enabled) {

        if (player == null || namespace == null || path == null || uuid == null) {
            return;
        }

        ResourceLocation id = new ResourceLocation(namespace, path);

        BuiltInRegistries.ATTRIBUTE.getOptional(id).ifPresent(attribute ->
                RegistryAttributes.applyPermanentAddition(
                        player,
                        attribute,
                        amount,
                        uuid,
                        enabled));
    }
}