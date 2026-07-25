package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.platform.ReachAttributeBridge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeRegistryAttributes {
    private static final DeferredRegister<Attribute> REGISTER =
            DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, Constants.MOD_ID);

    static {
        RegistryAttributes.values().forEach(attribute ->
                REGISTER.register(RegistryAttributes.getId(attribute).getPath(), () -> attribute));
    }

    private ForgeRegistryAttributes() {
    }

    public static void load(IEventBus eventBus) {
        REGISTER.register(eventBus);
        ReachAttributeBridge.setApplier((player, passive, passiveLevel) -> {
            if (passive == RegistryPassives.ENTITY_REACH && ForgeMod.ENTITY_REACH.isPresent()) {
                RegistryAttributes.applyPermanentAddition(
                        player,
                        ForgeMod.ENTITY_REACH.get(),
                        RegistryAttributes.passiveModifierAmount(passive, passiveLevel),
                        java.util.UUID.fromString(passive.attributeUuid),
                        passiveLevel > 0);
            } else if (passive == RegistryPassives.BLOCK_REACH && ForgeMod.BLOCK_REACH.isPresent()) {
                RegistryAttributes.applyPermanentAddition(
                        player,
                        ForgeMod.BLOCK_REACH.get(),
                        RegistryAttributes.passiveModifierAmount(passive, passiveLevel),
                        java.util.UUID.fromString(passive.attributeUuid),
                        passiveLevel > 0);
            }
        });
    }

    public static void addEntityAttributes(EntityAttributeModificationEvent event) {
        RegistryAttributes.values().forEach(attribute -> event.add(EntityType.PLAYER, attribute));
    }
}
