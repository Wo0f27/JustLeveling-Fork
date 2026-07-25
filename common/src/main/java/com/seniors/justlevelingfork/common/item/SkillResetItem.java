package com.seniors.justlevelingfork.common.item;

import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SkillResetItem extends Item {
    private static SkillResetHandler handler = SkillResetItem::resetWithPlayerProgressService;

    public SkillResetItem(Properties properties) {
        super(properties);
    }

    public static void setHandler(SkillResetHandler handler) {
        SkillResetItem.handler = Optional.ofNullable(handler).orElse(SkillResetItem::resetWithPlayerProgressService);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!handler.resetSkills(level, player, stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(Component.translatable("message.justlevelingfork.skills_reset"), true);
        return InteractionResultHolder.consume(stack);
    }

    @FunctionalInterface
    public interface SkillResetHandler {
        boolean resetSkills(Level level, Player player, ItemStack stack);
    }

    private static boolean resetWithPlayerProgressService(Level level, Player player, ItemStack stack) {
        return player instanceof ServerPlayer serverPlayer && PlayerProgressService.resetSkills(serverPlayer);
    }
}
