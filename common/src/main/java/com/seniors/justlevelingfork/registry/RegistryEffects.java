package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class RegistryEffects {
    private static BooleanSupplier showPotionsHud = CommonConfigService::showPotionsHud;

    private RegistryEffects() {
    }

    public static void setShowPotionsHud(BooleanSupplier showPotionsHud) {
        RegistryEffects.showPotionsHud =
                Optional.ofNullable(showPotionsHud).orElse(CommonConfigService::showPotionsHud);
    }

    public static class AddEffect {
        public final ServerPlayer player;
        public final boolean toggle;
        public final MobEffect effect;

        public AddEffect(ServerPlayer player, boolean toggle, MobEffect mobEffect) {
            this.player = player;
            this.toggle = toggle;
            this.effect = mobEffect;
        }

        public void add(int duration) {
            add(duration, 0);
        }

        public void add(int duration, int amplifier) {
            if (this.toggle) {
                MobEffectInstance instance = new MobEffectInstance(
                        this.effect, duration, amplifier, false, false, showPotionsHud.getAsBoolean());
                this.player.addEffect(instance);
            }
        }
    }
}
