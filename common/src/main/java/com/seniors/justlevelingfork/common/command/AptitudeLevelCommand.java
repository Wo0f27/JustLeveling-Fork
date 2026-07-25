package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.command.arguments.AptitudeArgument;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class AptitudeLevelCommand {
    private AptitudeLevelCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aptitudes")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("aptitude", AptitudeArgument.getArgument())
                                .suggests(AptitudeArgument::listSuggestions)
                                .then(Commands.literal("get")
                                        .executes(context -> getAptitude(
                                                context,
                                                EntityArgument.getPlayer(context, "player"),
                                                AptitudeArgument.getAptitude(context, "aptitude"))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> setAptitude(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player"),
                                                        AptitudeArgument.getAptitude(context, "aptitude"),
                                                        IntegerArgumentType.getInteger(context, "level")))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> addAptitude(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player"),
                                                        AptitudeArgument.getAptitude(context, "aptitude"),
                                                        IntegerArgumentType.getInteger(context, "level")))))
                                .then(Commands.literal("subtract")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> subtractAptitude(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player"),
                                                        AptitudeArgument.getAptitude(context, "aptitude"),
                                                        IntegerArgumentType.getInteger(context, "level"))))))));
    }

    private static int getAptitude(CommandContext<CommandSourceStack> context, ServerPlayer player, String aptitudeName) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName);
        if (aptitude == null) {
            sendUnknownAptitude(context, aptitudeName);
            return 0;
        }

        return PlayerProgressService.get(player)
                .map(progress -> {
                    sendGetMessage(context, player, aptitude, progress.getAptitudeLevel(aptitude));
                    return Command.SINGLE_SUCCESS;
                })
                .orElseGet(() -> {
                    context.getSource().sendFailure(Component.translatable("commands.message.capability.not_found"));
                    return 0;
                });
    }

    private static int setAptitude(
            CommandContext<CommandSourceStack> context, ServerPlayer player, String aptitudeName, int level) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName);
        if (aptitude == null) {
            sendUnknownAptitude(context, aptitudeName);
            return 0;
        }

        if (!PlayerProgressService.setAptitudeLevel(player, aptitude, level, CommonConfigService.aptitudeMaxLevel())) {
            context.getSource().sendFailure(Component.translatable("commands.message.capability.not_found"));
            return 0;
        }

        return getAptitude(context, player, aptitudeName);
    }

    private static int addAptitude(
            CommandContext<CommandSourceStack> context, ServerPlayer player, String aptitudeName, int level) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName);
        if (aptitude == null) {
            sendUnknownAptitude(context, aptitudeName);
            return 0;
        }

        int targetLevel = PlayerProgressService.get(player)
                .map(progress -> saturatingAdd(progress.getAptitudeLevel(aptitude), level))
                .orElse(1);
        return setAptitude(context, player, aptitudeName, targetLevel);
    }

    private static int subtractAptitude(
            CommandContext<CommandSourceStack> context, ServerPlayer player, String aptitudeName, int level) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName);
        if (aptitude == null) {
            sendUnknownAptitude(context, aptitudeName);
            return 0;
        }

        int targetLevel = PlayerProgressService.get(player)
                .map(progress -> saturatingSubtract(progress.getAptitudeLevel(aptitude), level))
                .orElse(1);
        return setAptitude(context, player, aptitudeName, targetLevel);
    }

    private static int saturatingAdd(int currentLevel, int amount) {
        return (int) Math.min((long) currentLevel + amount, CommonConfigService.aptitudeMaxLevel());
    }

    private static int saturatingSubtract(int currentLevel, int amount) {
        return (int) Math.max((long) currentLevel - amount, 1L);
    }

    private static void sendUnknownAptitude(CommandContext<CommandSourceStack> context, String aptitudeName) {
        context.getSource().sendFailure(Component.translatable("commands.argument.aptitude.not_found", aptitudeName));
    }

    private static void sendGetMessage(
            CommandContext<CommandSourceStack> context, ServerPlayer player, Aptitude aptitude, int level) {
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "commands.message.aptitude.get",
                        player.getName().copy().withStyle(ChatFormatting.BOLD),
                        Component.literal(String.valueOf(level)).withStyle(ChatFormatting.BOLD),
                        Component.translatable(aptitude.getKey()).withStyle(ChatFormatting.BOLD)),
                false);
    }
}
