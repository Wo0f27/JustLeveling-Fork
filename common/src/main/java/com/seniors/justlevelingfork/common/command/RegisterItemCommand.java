package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seniors.justlevelingfork.common.command.arguments.AptitudeArgument;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.config.models.LockItem;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RegisterItemCommand {
    private RegisterItemCommand() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            LockItemStore lockItemStore,
            CommandSync sync) {
        dispatcher.register(Commands.literal("registeritem")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("aptitude", AptitudeArgument.getArgument())
                        .suggests(AptitudeArgument::listSuggestions)
                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes(context -> execute(context, lockItemStore, sync)))));
    }

    private static int execute(
            CommandContext<CommandSourceStack> context,
            LockItemStore lockItemStore,
            CommandSync sync)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No item detected in main hand."));
            return 0;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String aptitudeName = AptitudeArgument.getAptitude(context, "aptitude");
        int level = IntegerArgumentType.getInteger(context, "level");
        if (level == 1) {
            context.getSource().sendFailure(Component.literal(
                    "Item requirements must be at least level 2, or 0 to remove a requirement."));
            return 0;
        }
        if (level > CommonConfigService.aptitudeMaxLevel()) {
            context.getSource().sendFailure(Component.literal(
                    "Item requirements cannot exceed the configured aptitude maximum of "
                            + CommonConfigService.aptitudeMaxLevel()
                            + "."));
            return 0;
        }
        LockItemRegistration.Result result =
                LockItemRegistration.apply(lockItemStore.lockItems(), itemId.toString(), aptitudeName, level);

        if (result.changed()) {
            lockItemStore.save();
            sync.sync(context.getSource());
        } else {
            context.getSource().sendFailure(Component.literal(result.message()));
            return 0;
        }

        player.sendSystemMessage(Component.literal(result.message()));
        return Command.SINGLE_SUCCESS;
    }

    public interface LockItemStore {
        List<LockItem> lockItems();

        void save();
    }

    @FunctionalInterface
    public interface CommandSync {
        void sync(CommandSourceStack source);
    }
}
