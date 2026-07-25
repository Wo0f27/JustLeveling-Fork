package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.common.player.SkillMessageService;
import com.seniors.justlevelingfork.registry.RegistryEffects;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class KillSkillEffects {
    private KillSkillEffects() {
    }

    public static void afterDeathLoot(LivingEntity defeated, DamageSource source, List<ItemStack> equipmentBeforeLoot) {
        if (defeated == null || defeated.level().isClientSide || !(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        applyFightingSpirit(defeated, player);
        applyLifeEater(player);
        applyLuckyDrop(defeated, player, equipmentBeforeLoot == null ? List.of() : equipmentBeforeLoot);
    }

    private static void applyFightingSpirit(LivingEntity defeated, ServerPlayer player) {
        if (defeated instanceof Player) {
            return;
        }

        new RegistryEffects.AddEffect(
                        player,
                        PlayerProgressService.isSkillEnabled(player, RegistrySkills.FIGHTING_SPIRIT),
                        MobEffects.DAMAGE_BOOST)
                .add(
                        (int) (10.0D + 20.0D * RegistrySkills.FIGHTING_SPIRIT.getValue()[1]),
                        (int) (RegistrySkills.FIGHTING_SPIRIT.getValue()[0] - 1.0D));
    }

    private static void applyLifeEater(ServerPlayer player) {
        if (PlayerProgressService.isSkillEnabled(player, RegistrySkills.LIFE_EATER)) {
            player.heal((float) RegistrySkills.LIFE_EATER.getValue()[0]);
        }
    }

    private static void applyLuckyDrop(
            LivingEntity defeated, ServerPlayer player, List<ItemStack> equipmentBeforeLoot) {
        if (defeated instanceof Player
                || !PlayerProgressService.isSkillEnabled(player, RegistrySkills.LUCKY_DROP)
                || ThreadLocalRandom.current().nextInt(Math.max(1, (int) RegistrySkills.LUCKY_DROP.getValue()[0])) != 0) {
            return;
        }

        int multiplier = Math.max(1, (int) RegistrySkills.LUCKY_DROP.getValue()[1]);
        SkillMessageService.send(player, "overlay.skill.justlevelingfork.lucky_drop", multiplier);
        AABB nearbyDrops = defeated.getBoundingBox().inflate(1.0D);
        for (ItemEntity itemEntity : defeated.level().getEntitiesOfClass(ItemEntity.class, nearbyDrops)) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty() || stack.getMaxStackSize() <= 1 || isEquipmentDrop(stack, equipmentBeforeLoot)) {
                continue;
            }

            stack.setCount(stack.getCount() * multiplier);
            itemEntity.setItem(stack);
        }
    }

    private static boolean isEquipmentDrop(ItemStack stack, List<ItemStack> equipmentBeforeLoot) {
        return equipmentBeforeLoot.stream()
                .filter(equipment -> !equipment.isEmpty())
                .anyMatch(equipment -> ItemStack.isSameItemSameTags(stack, equipment));
    }
}
