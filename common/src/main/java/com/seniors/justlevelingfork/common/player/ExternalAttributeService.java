package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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

        if (player == null
                || namespace == null
                || path == null
                || uuid == null) {

            return;
        }

        ResourceLocation id =
                new ResourceLocation(
                        namespace,
                        path);

        BuiltInRegistries.ATTRIBUTE
                .getOptional(id)
                .ifPresent(attribute ->
                        RegistryAttributes
                                .applyPermanentAddition(
                                        player,
                                        attribute,
                                        amount,
                                        uuid,
                                        enabled));
    }

    /**
     * Applies a transient MULTIPLY_TOTAL modifier to an
     * attribute owned by another mod.
     *
     * Example:
     *
     * -0.15 = 15% reduction
     *  0.20 = 20% increase
     *
     * This is intended for dynamic state such as currently
     * equipped non-proficient gear.
     */
    public static void applyTransientMultiplier(
            Player player,
            String namespace,
            String path,
            double amount,
            UUID uuid,
            boolean enabled) {

        if (player == null
                || namespace == null
                || path == null
                || uuid == null) {

            return;
        }

        ResourceLocation id =
                new ResourceLocation(
                        namespace,
                        path);

        BuiltInRegistries.ATTRIBUTE
                .getOptional(id)
                .ifPresent(attribute -> {

                    AttributeInstance instance =
                            player.getAttribute(
                                    attribute);

                    if (instance == null) {
                        return;
                    }

                    AttributeModifier existing =
                            instance.getModifier(
                                    uuid);

                    if (!enabled) {

                        if (existing != null) {

                            instance.removeModifier(
                                    existing);
                        }

                        return;
                    }

                    /*
                     * Don't continuously remove/re-add an
                     * unchanged modifier every refresh.
                     */
                    if (existing != null
                            && Double.compare(
                            existing.getAmount(),
                            amount) == 0
                            && existing.getOperation()
                            == AttributeModifier.Operation
                            .MULTIPLY_TOTAL) {

                        return;
                    }

                    if (existing != null) {

                        instance.removeModifier(
                                existing);
                    }

                    instance.addTransientModifier(
                            new AttributeModifier(
                                    uuid,
                                    Constants.MOD_ID
                                            + ":external_"
                                            + namespace
                                            + "_"
                                            + path,
                                    amount,
                                    AttributeModifier.Operation
                                            .MULTIPLY_TOTAL));
                });
    }

    public static double getValue(
            Player player,
            String namespace,
            String path,
            double fallback) {

        if (player == null
                || namespace == null
                || path == null) {

            return fallback;
        }

        ResourceLocation id =
                new ResourceLocation(
                        namespace,
                        path);

        return BuiltInRegistries.ATTRIBUTE
                .getOptional(id)
                .map(attribute -> {

                    AttributeInstance instance =
                            player.getAttribute(
                                    attribute);

                    return instance == null
                            ? fallback
                            : instance.getValue();
                })
                .orElse(fallback);
    }
    public static void applyTransientAddition(
            Player player,
            String namespace,
            String path,
            double amount,
            UUID uuid,
            boolean enabled) {

        if (player == null
                || namespace == null
                || path == null
                || uuid == null) {

            return;
        }

        ResourceLocation id =
                new ResourceLocation(
                        namespace,
                        path);

        BuiltInRegistries.ATTRIBUTE
                .getOptional(id)
                .ifPresent(attribute -> {

                    AttributeInstance instance =
                            player.getAttribute(
                                    attribute);

                    if (instance == null) {
                        return;
                    }

                    AttributeModifier existing =
                            instance.getModifier(
                                    uuid);

                    /*
                     * Penalty is no longer active.
                     */
                    if (!enabled) {

                        if (existing != null) {

                            instance.removeModifier(
                                    existing);
                        }

                        return;
                    }

                    /*
                     * Avoid replacing an unchanged modifier
                     * every five ticks.
                     */
                    if (existing != null
                            && Double.compare(
                            existing.getAmount(),
                            amount) == 0
                            && existing.getOperation()
                            == AttributeModifier.Operation
                            .ADDITION) {

                        return;
                    }

                    if (existing != null) {

                        instance.removeModifier(
                                existing);
                    }

                    instance.addTransientModifier(
                            new AttributeModifier(
                                    uuid,
                                    Constants.MOD_ID
                                            + ":external_"
                                            + namespace
                                            + "_"
                                            + path,
                                    amount,
                                    AttributeModifier.Operation
                                            .ADDITION));
                });
    }
}