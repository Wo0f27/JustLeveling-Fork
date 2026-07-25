package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class ConfigLimitCommands {
    private ConfigLimitCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, ConfigLimitStore store) {
        dispatcher.register(Commands.literal("updateaptitudelevel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(
                                "level",
                                IntegerArgumentType.integer(2, CommonConfigService.MAX_APTITUDE_LEVEL))
                        .executes(context -> updateAptitudeMaxLevel(context, store))));
        dispatcher.register(Commands.literal("globallimit")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(
                                "level",
                                IntegerArgumentType.integer(32, CommonConfigService.MAX_GLOBAL_LEVEL))
                        .executes(context -> updatePlayersMaxGlobalLevel(context, store))));
    }

    private static int updateAptitudeMaxLevel(CommandContext<CommandSourceStack> context, ConfigLimitStore store) {
        int level = IntegerArgumentType.getInteger(context, "level");
        store.setAptitudeMaxLevel(level);
        store.sync(context.getSource());
        context.getSource().sendSystemMessage(
                Component.literal(String.format("Updating aptitudeMaxLevel, new level: %d", level)));
        return Command.SINGLE_SUCCESS;
    }

    private static int updatePlayersMaxGlobalLevel(CommandContext<CommandSourceStack> context, ConfigLimitStore store) {
        int level = IntegerArgumentType.getInteger(context, "level");
        store.setPlayersMaxGlobalLevel(level);
        store.sync(context.getSource());
        context.getSource().sendSystemMessage(
                Component.literal(String.format("Updating playersMaxGlobalLevel, new level: %d", level)));
        return Command.SINGLE_SUCCESS;
    }

    public interface ConfigLimitStore {
        void setAptitudeMaxLevel(int level);

        void setPlayersMaxGlobalLevel(int level);

        void sync(CommandSourceStack source);
    }
}
