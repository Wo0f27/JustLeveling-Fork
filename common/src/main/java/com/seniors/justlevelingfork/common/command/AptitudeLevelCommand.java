package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Locale;
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
                        .then(Commands.argument("aptitude", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                                    RegistryAptitudes.values().stream()
                                            .map(aptitude -> capitalize(aptitude.getName()))
                                            .filter(name -> remaining.isEmpty()
                                                    || name.toLowerCase(Locale.ROOT).contains(remaining))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(Commands.literal("get")
                                        .executes(context -> getAptitude(
                                                context,
                                                EntityArgument.getPlayer(context, "player"),
                                                context.getArgument("aptitude", String.class))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> setAptitude(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player"),
                                                        context.getArgument("aptitude", String.class),
                                                        IntegerArgumentType.getInteger(context, "level")))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> addAptitude(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player"),
                                                        context.getArgument("aptitude", String.class),
                                                        IntegerArgumentType.getInteger(context, "level")))))
                                .then(Commands.literal("subtract")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> subtractAptitude(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player"),
                                                        context.getArgument("aptitude", String.class),
                                                        IntegerArgumentType.getInteger(context, "level"))))))));
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private static int getAptitude(CommandContext<CommandSourceStack> context, ServerPlayer player, String aptitudeName) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName);
        if (aptitude == null) {
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
            return 0;
        }

        int targetLevel = PlayerProgressService.get(player)
                .map(progress -> progress.getAptitudeLevel(aptitude) + level)
                .orElse(1);
        return setAptitude(context, player, aptitudeName, targetLevel);
    }

    private static int subtractAptitude(
            CommandContext<CommandSourceStack> context, ServerPlayer player, String aptitudeName, int level) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName);
        if (aptitude == null) {
            return 0;
        }

        int targetLevel = PlayerProgressService.get(player)
                .map(progress -> progress.getAptitudeLevel(aptitude) - level)
                .orElse(1);
        return setAptitude(context, player, aptitudeName, targetLevel);
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
