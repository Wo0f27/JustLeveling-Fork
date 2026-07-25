package com.seniors.justlevelingfork.common.event;

import com.seniors.justlevelingfork.common.HorseEquipmentRestrictions;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class InteractionRestrictions {
    private InteractionRestrictions() {
    }

    public static boolean canUseItem(Player player, ItemStack stack) {
        return !(player instanceof ServerPlayer serverPlayer)
                || serverPlayer.isCreative()
                || HandlerAptitude.canUseItem(serverPlayer, stack);
    }

    public static boolean canUseBlock(Player player, Block block, ItemStack stack) {
        return !(player instanceof ServerPlayer serverPlayer)
                || serverPlayer.isCreative()
                || (HandlerAptitude.canUseBlock(serverPlayer, block)
                        && HandlerAptitude.canUseItem(serverPlayer, stack));
    }

    public static boolean canAttackBlock(Player player, Block block, ItemStack stack) {
        return canUseBlock(player, block, stack);
    }

    public static boolean canUseEntity(Player player, Entity entity, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.isCreative()) {
            return true;
        }

        if (!HandlerAptitude.canUseEntity(serverPlayer, entity) || !HandlerAptitude.canUseItem(serverPlayer, stack)) {
            return false;
        }

        return !(entity instanceof AbstractHorse horse) || HorseEquipmentRestrictions.canEquip(player, horse, stack);
    }

    public static boolean canAttackEntity(Player player, Entity entity, ItemStack stack) {
        return !(player instanceof ServerPlayer serverPlayer)
                || serverPlayer.isCreative()
                || (HandlerAptitude.canUseEntity(serverPlayer, entity)
                        && HandlerAptitude.canUseItem(serverPlayer, stack));
    }
}
