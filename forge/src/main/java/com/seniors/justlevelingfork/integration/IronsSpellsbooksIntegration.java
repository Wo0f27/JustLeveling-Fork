package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.common.proficiency.EquipmentPenaltyProfile;
import com.seniors.justlevelingfork.common.proficiency.EquipmentPenaltyService;
import com.seniors.justlevelingfork.handler.HandlerAptitude;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import io.redspace.ironsspellbooks.api.events.SpellCooldownAddedEvent;

public class IronsSpellsbooksIntegration {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSpellCastEvent(
            SpellPreCastEvent event) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        String spellId =
                event.getSpellId();

        if (ForgeIntegrationConfig.logSpellIds()) {

            player.sendSystemMessage(
                    Component.literal(
                            String.format(
                                    "[JLFork] >> Spell ID: %s",
                                    spellId)));
        }

        /*
         * Existing JLF spell-use restriction.
         */
        if (!player.isCreative()
                && !HandlerAptitude
                .canUseSpecificId(
                        player,
                        spellId)) {

            event.setCanceled(true);
            return;
        }

        /*
         * Some cast sources do not consume mana.
         *
         * In those cases there is nothing for the
         * proficiency system to surcharge.
         */
        if (!event.getCastSource()
                .consumesMana()) {

            return;
        }

        MagicData magicData =
                MagicData
                        .getPlayerMagicData(
                                player);

        /*
         * Iron's does not charge mana again for recasts.
         *
         * Match that behavior here so JLF does not prevent
         * a valid recast because of a cost that will never
         * actually be deducted.
         */
        if (magicData
                .getPlayerRecasts()
                .hasRecastForSpell(
                        spellId)) {

            return;
        }

        EquipmentPenaltyProfile profile =
                EquipmentPenaltyService
                        .resolve(player);

        double manaPenalty =
                profile.manaCostIncrease();

        if (manaPenalty <= 0.0D) {
            return;
        }

        var spell =
                SpellRegistry.getSpell(
                        spellId);

        if (spell == SpellRegistry.none()) {
            return;
        }

        int baseManaCost =
                spell.getManaCost(
                        event.getSpellLevel());

        int adjustedManaCost =
                adjustedManaCost(
                        baseManaCost,
                        manaPenalty);

        /*
         * Iron's already checked affordability using the
         * normal mana cost before firing SpellPreCastEvent.
         *
         * We perform the additional JLF affordability check
         * here before the cast actually starts.
         */
        if (magicData.getMana()
                < adjustedManaCost) {

            event.setCanceled(true);

            player.displayClientMessage(
                    Component.literal(
                            "Not enough mana: "
                                    + adjustedManaCost
                                    + " required while using "
                                    + "non-proficient equipment."),
                    true);
        }
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSpellOnCast(
            SpellOnCastEvent event) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        if (!event.getCastSource()
                .consumesMana()) {

            return;
        }

        MagicData magicData =
                MagicData
                        .getPlayerMagicData(
                                player);

        /*
         * Iron's intentionally does not charge mana again
         * while processing a spell recast.
         */
        if (magicData
                .getPlayerRecasts()
                .hasRecastForSpell(
                        event.getSpellId())) {

            return;
        }

        EquipmentPenaltyProfile profile =
                EquipmentPenaltyService
                        .resolve(player);

        double manaPenalty =
                profile.manaCostIncrease();

        if (manaPenalty <= 0.0D) {
            return;
        }

        int originalManaCost =
                event.getManaCost();

        int adjustedManaCost =
                adjustedManaCost(
                        originalManaCost,
                        manaPenalty);

        event.setManaCost(
                adjustedManaCost);

        if (ForgeIntegrationConfig.logSpellIds()) {

            player.sendSystemMessage(
                    Component.literal(
                            String.format(
                                    "[JLFork] >> Mana Cost: %d -> %d (+%.0f%%)",
                                    originalManaCost,
                                    adjustedManaCost,
                                    manaPenalty * 100.0D)));
        }
    }

    private static int adjustedManaCost(
            int baseManaCost,
            double penalty) {

        if (baseManaCost <= 0) {
            return Math.max(
                    0,
                    baseManaCost);
        }

        double safePenalty =
                Math.max(
                        0.0D,
                        Math.min(
                                penalty,
                                0.95D));

        double result =
                Math.ceil(
                        baseManaCost
                                * (1.0D
                                + safePenalty));

        if (result
                >= Integer.MAX_VALUE) {

            return Integer.MAX_VALUE;
        }

        return (int) result;
    }

    private static int adjustedCooldown(
            int baseCooldown,
            double penalty) {

        if (baseCooldown <= 0) {

            return Math.max(
                    0,
                    baseCooldown);
        }

        double safePenalty =
                Math.max(
                        0.0D,
                        Math.min(
                                penalty,
                                0.95D));

        double result =
                Math.ceil(
                        baseCooldown
                                * (1.0D
                                + safePenalty));

        if (result
                >= Integer.MAX_VALUE) {

            return Integer.MAX_VALUE;
        }

        return (int) result;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCooldownAdded(
            SpellCooldownAddedEvent.Pre event) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        EquipmentPenaltyProfile profile =
                EquipmentPenaltyService
                        .resolve(player);

        double cooldownPenalty =
                profile.spellCooldownIncrease();

        if (cooldownPenalty <= 0.0D) {
            return;
        }

        int originalCooldown =
                event.getEffectiveCooldown();

        int adjustedCooldown =
                adjustedCooldown(
                        originalCooldown,
                        cooldownPenalty);

        event.setEffectiveCooldown(
                adjustedCooldown);

        if (ForgeIntegrationConfig.logSpellIds()) {

            player.sendSystemMessage(
                    Component.literal(
                            String.format(
                                    "[JLFork] >> Spell Cooldown: %d -> %d ticks (+%.0f%%)",
                                    originalCooldown,
                                    adjustedCooldown,
                                    cooldownPenalty
                                            * 100.0D)));
        }
    }
}