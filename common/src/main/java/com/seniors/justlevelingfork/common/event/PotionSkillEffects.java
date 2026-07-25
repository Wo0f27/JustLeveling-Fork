package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.PotionItem;

public final class PotionSkillEffects {
    private PotionSkillEffects() {
    }

    public static MobEffectInstance adjustAddedEffect(LivingEntity target, MobEffectInstance effect) {
        if (!(target instanceof ServerPlayer player) || effect == null) {
            return effect;
        }

        int duration = effect.getDuration();
        int amplifier = effect.getAmplifier();

        if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL
                && PlayerProgressService.isSkillEnabled(player, RegistrySkills.LION_HEART)) {
            duration -= (int) (effect.getDuration() * RegistrySkills.LION_HEART.getValue()[0] / 100.0D);
        }

        if (effect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL && isDrinkingPotion(player)) {
            if (PlayerProgressService.isSkillEnabled(player, RegistrySkills.ALCHEMY_MANIPULATION)) {
                amplifier += (int) RegistrySkills.ALCHEMY_MANIPULATION.getValue()[0];
            }
            duration += (int) (player.getAttributeValue(RegistryAttributes.BENEFICIAL_EFFECT) * 20.0D);
        }

        if (duration == effect.getDuration() && amplifier == effect.getAmplifier()) {
            return effect;
        }

        return new MobEffectInstance(
                effect.getEffect(),
                Math.max(0, duration),
                Math.max(0, amplifier),
                effect.isAmbient(),
                effect.isVisible(),
                effect.showIcon());
    }

    private static boolean isDrinkingPotion(ServerPlayer player) {
        return player.isUsingItem()
                && (player.getMainHandItem().getItem() instanceof PotionItem
                        || player.getOffhandItem().getItem() instanceof PotionItem);
    }
}
