package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;

public final class AbilityDerivedAttributeService {

    // Strength - Vanilla
    private static final UUID STR_ATTACK_DAMAGE_UUID =
            UUID.fromString("a9ff4a9d-bfee-48b5-883a-217a98175549");

    private static final UUID STR_ATTACK_KNOCKBACK_UUID =
            UUID.fromString("e39006fe-ba10-4175-9c12-c8377e562fd0");

    // Strength - Apothic
    private static final UUID STR_ARMOR_PIERCE_UUID =
            UUID.fromString("b872356f-ce39-4c29-a1d6-b55cb6db9074");

    // Dexterity - Apothic
    private static final UUID DEX_DODGE_CHANCE_UUID =
            UUID.fromString("436632ee-d557-4fd8-adfe-a14bf9ff58eb");

    private static final UUID DEX_ARROW_DAMAGE_UUID =
            UUID.fromString("2d81a080-d791-4408-86e8-ddccfc5149d6");

    private static final UUID DEX_ARROW_VELOCITY_UUID =
            UUID.fromString("efd03779-0d97-430d-9fa8-517586ae4f57");

    private static final UUID DEX_DRAW_SPEED_UUID =
            UUID.fromString("6889762e-e03a-4341-a04f-ec74f21bcb28");

    private static final UUID DEX_CRIT_CHANCE_UUID =
            UUID.fromString("cf052963-3294-4373-89c9-4d71b1ef584e");

    // Constitution - Vanilla
    private static final UUID CON_MAX_HEALTH_UUID =
            UUID.fromString("27caebe2-79ba-4302-9319-d8b124fa3a07");

    private static final UUID CON_KNOCKBACK_RESISTANCE_UUID =
            UUID.fromString("96ea6b30-2a31-46e9-94db-27dc99fd60e5");

    // Intelligence - Iron's Spells
    private static final UUID INT_MAX_MANA_UUID =
            UUID.fromString("044dc8c2-b9f2-4e40-8929-c609c9c97907");

    private static final UUID INT_CAST_TIME_REDUCTION_UUID =
            UUID.fromString("c3329c0d-677e-4ade-b845-2998b8ebc088");

    // Wisdom - Iron's Spells
    private static final UUID WIS_MANA_REGEN_UUID =
            UUID.fromString("8d623089-9ad9-406e-9c9c-cac92eee56e5");

    private static final UUID WIS_SPELL_RESIST_UUID =
            UUID.fromString("d78b3bb7-72b0-4d0b-a89e-edac8fa86523");

    // Charisma - Iron's Spells
    private static final UUID CHA_COOLDOWN_REDUCTION_UUID =
            UUID.fromString("23a50606-651e-4cfe-9ade-9165ff2a898d");

    private AbilityDerivedAttributeService() {
    }

    public static void refresh(ServerPlayer player, PlayerProgress progress) {
        if (player == null || progress == null) {
            return;
        }

        int strengthModifier = effectiveModifier(
                player,
                progress,
                RegistryAptitudes.STRENGTH);

        int dexterityModifier = effectiveModifier(
                player,
                progress,
                RegistryAptitudes.DEXTERITY);

        int constitutionModifier = effectiveModifier(
                player,
                progress,
                RegistryAptitudes.CONSTITUTION);

        int intelligenceModifier = effectiveModifier(
                player,
                progress,
                RegistryAptitudes.INTELLIGENCE);

        int wisdomModifier = effectiveModifier(
                player,
                progress,
                RegistryAptitudes.WISDOM);

        int charismaModifier = effectiveModifier(
                player,
                progress,
                RegistryAptitudes.CHARISMA);

        applyStrength(player, strengthModifier);
        applyDexterity(player, dexterityModifier);
        applyConstitution(player, constitutionModifier);
        applyIntelligence(player, intelligenceModifier);
        applyWisdom(player, wisdomModifier);
        applyCharisma(player, charismaModifier);
    }

    private static void applyStrength(ServerPlayer player, int modifier) {
        RegistryAttributes.applyPermanentAddition(
                player,
                Attributes.ATTACK_DAMAGE,
                modifier * 1.0D,
                STR_ATTACK_DAMAGE_UUID,
                modifier != 0);

        RegistryAttributes.applyPermanentAddition(
                player,
                Attributes.ATTACK_KNOCKBACK,
                modifier * 0.10D,
                STR_ATTACK_KNOCKBACK_UUID,
                modifier != 0);

        ExternalAttributeService.applyPermanentAddition(
                player,
                "attributeslib",
                "armor_pierce",
                modifier * 0.50D,
                STR_ARMOR_PIERCE_UUID,
                modifier != 0);
    }

    private static void applyDexterity(ServerPlayer player, int modifier) {
        ExternalAttributeService.applyPermanentAddition(
                player,
                "attributeslib",
                "dodge_chance",
                modifier * 0.01D,
                DEX_DODGE_CHANCE_UUID,
                modifier != 0);

        ExternalAttributeService.applyPermanentAddition(
                player,
                "attributeslib",
                "arrow_damage",
                modifier * 0.05D,
                DEX_ARROW_DAMAGE_UUID,
                modifier != 0);

        ExternalAttributeService.applyPermanentAddition(
                player,
                "attributeslib",
                "arrow_velocity",
                modifier * 0.03D,
                DEX_ARROW_VELOCITY_UUID,
                modifier != 0);

        ExternalAttributeService.applyPermanentAddition(
                player,
                "attributeslib",
                "draw_speed",
                modifier * 0.03D,
                DEX_DRAW_SPEED_UUID,
                modifier != 0);

        ExternalAttributeService.applyPermanentAddition(
                player,
                "attributeslib",
                "crit_chance",
                modifier * 0.01D,
                DEX_CRIT_CHANCE_UUID,
                modifier != 0);
    }

    private static void applyConstitution(ServerPlayer player, int modifier) {
        RegistryAttributes.applyPermanentAddition(
                player,
                Attributes.MAX_HEALTH,
                modifier * 2.0D,
                CON_MAX_HEALTH_UUID,
                modifier != 0);

        RegistryAttributes.applyPermanentAddition(
                player,
                Attributes.KNOCKBACK_RESISTANCE,
                modifier * 0.05D,
                CON_KNOCKBACK_RESISTANCE_UUID,
                modifier != 0);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyIntelligence(ServerPlayer player, int modifier) {
        ExternalAttributeService.applyPermanentAddition(
                player,
                "irons_spellbooks",
                "max_mana",
                modifier * 10.0D,
                INT_MAX_MANA_UUID,
                modifier != 0);

        ExternalAttributeService.applyPermanentAddition(
                player,
                "irons_spellbooks",
                "cast_time_reduction",
                modifier * 0.05D,
                INT_CAST_TIME_REDUCTION_UUID,
                modifier != 0);
    }

    private static void applyWisdom(ServerPlayer player, int modifier) {
        ExternalAttributeService.applyPermanentAddition(
                player,
                "irons_spellbooks",
                "mana_regen",
                modifier * 0.05D,
                WIS_MANA_REGEN_UUID,
                modifier != 0);

        ExternalAttributeService.applyPermanentAddition(
                player,
                "irons_spellbooks",
                "spell_resist",
                modifier * 0.05D,
                WIS_SPELL_RESIST_UUID,
                modifier != 0);
    }

    private static void applyCharisma(ServerPlayer player, int modifier) {
        ExternalAttributeService.applyPermanentAddition(
                player,
                "irons_spellbooks",
                "cooldown_reduction",
                modifier * 0.05D,
                CHA_COOLDOWN_REDUCTION_UUID,
                modifier != 0);
    }

    private static int effectiveModifier(
            ServerPlayer player,
            PlayerProgress progress,
            Aptitude aptitude) {

        int externalBonus =
                ExternalAbilityBonusService.getBonus(player, aptitude);

        return AbilityScoreService.abilityModifier(
                progress.getAptitudeLevel(aptitude),
                externalBonus);
    }
}