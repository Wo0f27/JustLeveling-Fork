package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

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

    private AbilityDerivedAttributeService() {
    }

    public static void refresh(ServerPlayer player, PlayerProgress progress) {
        if (player == null || progress == null) {
            return;
        }

        int strengthModifier = AbilityScoreService.abilityModifier(
                progress.getAptitudeLevel(RegistryAptitudes.STRENGTH));

        int dexterityModifier = AbilityScoreService.abilityModifier(
                progress.getAptitudeLevel(RegistryAptitudes.DEXTERITY));

        int constitutionModifier = AbilityScoreService.abilityModifier(
                progress.getAptitudeLevel(RegistryAptitudes.CONSTITUTION));

        applyStrength(player, strengthModifier);
        applyDexterity(player, dexterityModifier);
        applyConstitution(player, constitutionModifier);
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
}