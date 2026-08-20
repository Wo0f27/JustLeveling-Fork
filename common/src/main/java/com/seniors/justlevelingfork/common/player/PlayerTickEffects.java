package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistryEffects;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class PlayerTickEffects {
    private static final UUID ONE_HANDED_ATTACK_DAMAGE_ID =
            UUID.fromString("55550aa2-eff2-4a81-b92b-a1cb95f15555");
    private static final UUID DIAMOND_SKIN_ARMOR_ID =
            UUID.fromString("55550aa2-eff2-4a81-b92b-a1cb95f15556");

    private PlayerTickEffects() {
    }

    public static void apply(ServerPlayer player) {
        if (player == null) {
            return;
        }

        if (player.tickCount % 5 == 0) {
            PlayerProgressService.get(player).ifPresent(progress ->
                    ExternalAbilityBonusService.refreshDerivedAttributesIfChanged(
                            player,
                            progress));
        }

        dropLockedHeldItem(player, player.getMainHandItem());
        dropLockedHeldItem(player, player.getOffhandItem());
        dropLockedArmor(player);
        PlayerProgressService.tickCounterAttack(player);
        applySkillAttributes(player);
        clampHealth(player);
        applySkillEffects(player);
    }

    private static void dropLockedHeldItem(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !HandlerAptitude.shouldDropLockedItem(player, stack)) {
            return;
        }

        player.drop(stack.copy(), false);
        stack.setCount(0);
    }

    private static void dropLockedArmor(ServerPlayer player) {
        player.getInventory().armor.forEach(stack -> dropLockedHeldItem(player, stack));
    }

    private static void applySkillAttributes(ServerPlayer player) {
        boolean oneHanded = player.getOffhandItem().isEmpty()
                && PlayerProgressService.isSkillEnabled(player, RegistrySkills.ONE_HANDED);
        RegistryAttributes.applyPermanentAddition(
                player,
                Attributes.ATTACK_DAMAGE,
                RegistrySkills.ONE_HANDED.getValue()[0],
                ONE_HANDED_ATTACK_DAMAGE_ID,
                oneHanded);

        boolean diamondSkin = player.isShiftKeyDown()
                && PlayerProgressService.isSkillEnabled(player, RegistrySkills.DIAMOND_SKIN);
        RegistryAttributes.applyPermanentAddition(
                player,
                Attributes.ARMOR,
                RegistrySkills.DIAMOND_SKIN.getValue()[1],
                DIAMOND_SKIN_ARMOR_ID,
                diamondSkin);
    }

    private static void clampHealth(ServerPlayer player) {
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applySkillEffects(ServerPlayer player) {
        new RegistryEffects.AddEffect(
                        player,
                        PlayerProgressService.isSkillEnabled(player, RegistrySkills.CAT_EYES),
                        MobEffects.NIGHT_VISION)
                .add(210);
        new RegistryEffects.AddEffect(
                        player,
                        PlayerProgressService.isSkillEnabled(player, RegistrySkills.DIAMOND_SKIN),
                        MobEffects.DAMAGE_RESISTANCE)
                .add(210, (int) (RegistrySkills.DIAMOND_SKIN.getValue()[0] - 1.0D));
    }
}
