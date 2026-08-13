package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class AbilityDerivedAttributeService {

    private static final UUID STR_ATTACK_DAMAGE_UUID =
            UUID.fromString("a9ff4a9d-bfee-48b5-883a-217a98175549");

    private static final UUID STR_ATTACK_KNOCKBACK_UUID =
            UUID.fromString("e39006fe-ba10-4175-9c12-c8377e562fd0");

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

        int constitutionModifier = AbilityScoreService.abilityModifier(
                progress.getAptitudeLevel(RegistryAptitudes.CONSTITUTION));

        applyStrength(player, strengthModifier);
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