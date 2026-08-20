package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public final class ExternalAbilityBonusService {

    private ExternalAbilityBonusService() {
    }

    public static int getBonus(Player player, Aptitude aptitude) {
        Attribute attribute = getAttribute(aptitude);

        if (player == null || attribute == null) {
            return 0;
        }

        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) {
            return 0;
        }

        return (int) Math.round(instance.getValue());
    }

    public static void applyPermanentAddition(
            ServerPlayer player,
            Aptitude aptitude,
            int amount,
            UUID uuid,
            boolean enabled) {

        Attribute attribute = getAttribute(aptitude);

        if (player == null || attribute == null || uuid == null) {
            return;
        }

        RegistryAttributes.applyPermanentAddition(
                player,
                attribute,
                amount,
                uuid,
                enabled);

        PlayerProgressService.get(player).ifPresent(progress ->
                AbilityDerivedAttributeService.refresh(player, progress));
    }

    private static Attribute getAttribute(Aptitude aptitude) {
        if (aptitude == RegistryAptitudes.STRENGTH) {
            return RegistryAttributes.ABILITY_BONUS_STRENGTH;
        }

        if (aptitude == RegistryAptitudes.DEXTERITY) {
            return RegistryAttributes.ABILITY_BONUS_DEXTERITY;
        }

        if (aptitude == RegistryAptitudes.CONSTITUTION) {
            return RegistryAttributes.ABILITY_BONUS_CONSTITUTION;
        }

        if (aptitude == RegistryAptitudes.INTELLIGENCE) {
            return RegistryAttributes.ABILITY_BONUS_INTELLIGENCE;
        }

        if (aptitude == RegistryAptitudes.WISDOM) {
            return RegistryAttributes.ABILITY_BONUS_WISDOM;
        }

        if (aptitude == RegistryAptitudes.CHARISMA) {
            return RegistryAttributes.ABILITY_BONUS_CHARISMA;
        }

        return null;
    }
}