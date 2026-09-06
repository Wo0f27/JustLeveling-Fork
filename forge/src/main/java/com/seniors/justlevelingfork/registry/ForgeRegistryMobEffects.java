package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * JLF-owned mob effects.
 */
public final class ForgeRegistryMobEffects {

    private static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Constants.MOD_ID);

    /**
     * Alert's short reaction burst.
     *
     * This intentionally uses its own MobEffect and attribute-modifier UUID,
     * so it can coexist and stack with vanilla Speed instead of replacing it.
     */
    public static final RegistryObject<MobEffect> ALERT_REACTION =
            REGISTER.register(
                    "alert_reaction",
                    () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xE7C85A) {}
                            .addAttributeModifier(
                                    Attributes.MOVEMENT_SPEED,
                                    "c6339828-3c6d-4e17-a1cb-8640cb7e7c7d",
                                    0.20D,
                                    AttributeModifier.Operation.MULTIPLY_TOTAL));

    /**
     * Charger buildup indicator.
     *
     * The amplifier represents Momentum I-III. It deliberately has no
     * attribute modifiers; Charger gameplay reads the server-side movement
     * state directly. The literal display name is temporary until the final
     * Charger localization/icon polish checkpoint.
     */
    public static final RegistryObject<MobEffect> MOMENTUM =
            REGISTER.register(
                    "momentum",
                    () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xD39A45) {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("Momentum");
                        }
                    });

    private ForgeRegistryMobEffects() {
    }

    public static void load(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
