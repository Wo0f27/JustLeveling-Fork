package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.common.platform.ReachAttributeBridge;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.passive.Passive;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public final class PassiveAttributeService {

    private PassiveAttributeService() {
    }

    public static void apply(
            Player player,
            Passive passive,
            int passiveLevel) {

        if (player == null
                || passive == null) {

            return;
        }

        UUID uuid =
                UUID.fromString(
                        passive.attributeUuid);

        double amount =
                RegistryAttributes
                        .passiveModifierAmount(
                                passive,
                                passiveLevel);

        boolean enabled =
                passiveLevel > 0;

        /*
         * Canonical Apothic Attributes targets.
         */

        if (passive == RegistryPassives.PROJECTILE_DAMAGE) {

            /*
             * Legacy JLF configured this passive as 5.0
             * maximum, meaning five percentage points.
             *
             * Apothic Arrow Damage uses:
             *
             * 1.0 = 100%
             * 1.05 = 105%
             *
             * Therefore convert JLF percentage points to
             * the canonical fractional attribute value.
             */
            ExternalAttributeService
                    .applyPermanentAddition(
                            player,
                            "attributeslib",
                            "arrow_damage",
                            amount / 100.0D,
                            uuid,
                            enabled);

            return;
        }

        if (passive == RegistryPassives.BREAK_SPEED) {

            ExternalAttributeService
                    .applyPermanentAddition(
                            player,
                            "attributeslib",
                            "mining_speed",
                            amount,
                            uuid,
                            enabled);

            return;
        }

        if (passive == RegistryPassives.CRITICAL_DAMAGE) {

            ExternalAttributeService
                    .applyPermanentAddition(
                            player,
                            "attributeslib",
                            "crit_damage",
                            amount,
                            uuid,
                            enabled);

            return;
        }

        /*
         * Forge already owns the real player reach
         * attributes. The platform bridge applies these
         * passives to ForgeMod.ENTITY_REACH/BLOCK_REACH.
         */
        if (passive == RegistryPassives.ENTITY_REACH
                || passive == RegistryPassives.BLOCK_REACH) {

            ReachAttributeBridge.apply(
                    player,
                    passive,
                    passiveLevel);

            return;
        }

        /*
         * Vanilla attributes and genuinely JLF-specific
         * attributes continue through the normal path.
         */
        RegistryAttributes
                .applyPermanentAddition(
                        player,
                        passive.attribute,
                        amount,
                        uuid,
                        enabled);
    }
}