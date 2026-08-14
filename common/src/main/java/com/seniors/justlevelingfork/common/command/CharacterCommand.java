package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seniors.justlevelingfork.common.player.ClassProgressionService;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.seniors.justlevelingfork.common.player.AbilityScoreBonusService;
import com.seniors.justlevelingfork.common.player.AbilityScoreService;
import com.seniors.justlevelingfork.common.player.CharacterInitializationService;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import java.util.LinkedHashMap;
import com.seniors.justlevelingfork.common.player.PlayerProgress;

public final class CharacterCommand {

    private CharacterCommand() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("character")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.argument(
                                        "player",
                                        EntityArgument.player())

                                .then(Commands.literal("get")
                                        .executes(context ->
                                                showCharacter(
                                                        context,
                                                        EntityArgument.getPlayer(
                                                                context,
                                                                "player"))))

                                .then(Commands.literal("class")
                                        .then(Commands.literal("set")
                                                .then(Commands.argument(
                                                                "class",
                                                                StringArgumentType.word())

                                                        .suggests(
                                                                CharacterCommand::suggestClasses)

                                                        .then(Commands.argument(
                                                                        "level",
                                                                        IntegerArgumentType.integer(
                                                                                0,
                                                                                ClassProgressionService.MAX_CHARACTER_LEVEL))

                                                                .executes(context ->
                                                                        setClassLevel(
                                                                                context,
                                                                                EntityArgument.getPlayer(
                                                                                        context,
                                                                                        "player"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "class"),
                                                                                IntegerArgumentType.getInteger(
                                                                                        context,
                                                                                        "level")))))))

                                .then(Commands.literal("abilities")

                                        .then(Commands.literal("get")
                                                .executes(context ->
                                                        showAbilities(
                                                                context,
                                                                EntityArgument.getPlayer(
                                                                        context,
                                                                        "player"))))

                                        .then(Commands.literal("randomize")
                                                .executes(context ->
                                                        randomizeAbilities(
                                                                context,
                                                                EntityArgument.getPlayer(
                                                                        context,
                                                                        "player"))))

                                        .then(Commands.literal("recommend")
                                                .executes(context ->
                                                        recommendAbilities(
                                                                context,
                                                                EntityArgument.getPlayer(
                                                                        context,
                                                                        "player"))))

                                        .then(Commands.literal("set")

                                                .then(Commands.argument(
                                                                "strength",
                                                                IntegerArgumentType.integer(10, 15))

                                                        .then(Commands.argument(
                                                                        "dexterity",
                                                                        IntegerArgumentType.integer(10, 15))

                                                                .then(Commands.argument(
                                                                                "constitution",
                                                                                IntegerArgumentType.integer(10, 15))

                                                                        .then(Commands.argument(
                                                                                        "intelligence",
                                                                                        IntegerArgumentType.integer(10, 15))

                                                                                .then(Commands.argument(
                                                                                                "wisdom",
                                                                                                IntegerArgumentType.integer(10, 15))

                                                                                        .then(Commands.argument(
                                                                                                        "charisma",
                                                                                                        IntegerArgumentType.integer(10, 15))

                                                                                                .executes(context ->
                                                                                                        setStartingAbilities(
                                                                                                                context,
                                                                                                                EntityArgument.getPlayer(
                                                                                                                        context,
                                                                                                                        "player"),
                                                                                                                IntegerArgumentType.getInteger(
                                                                                                                        context,
                                                                                                                        "strength"),
                                                                                                                IntegerArgumentType.getInteger(
                                                                                                                        context,
                                                                                                                        "dexterity"),
                                                                                                                IntegerArgumentType.getInteger(
                                                                                                                        context,
                                                                                                                        "constitution"),
                                                                                                                IntegerArgumentType.getInteger(
                                                                                                                        context,
                                                                                                                        "intelligence"),
                                                                                                                IntegerArgumentType.getInteger(
                                                                                                                        context,
                                                                                                                        "wisdom"),
                                                                                                                IntegerArgumentType.getInteger(
                                                                                                                        context,
                                                                                                                        "charisma"))))))))))

                                        .then(Commands.literal("reset")
                                                .executes(context ->
                                                        resetAbilities(
                                                                context,
                                                                EntityArgument.getPlayer(
                                                                        context,
                                                                        "player")))))));

    }

    private static int setClassLevel(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            String className,
            int level) {

        ResourceLocation classId =
                RegistryClasses.get(className);

        if (classId == null) {
            context.getSource().sendFailure(
                    Component.literal(
                            "Unknown class: " + className));
            return 0;
        }

        if (!ClassProgressionService.setClassLevel(
                player,
                classId,
                level)) {

            context.getSource().sendFailure(
                    Component.literal(
                            "Could not set class level. "
                                    + "The character may exceed level "
                                    + ClassProgressionService.MAX_CHARACTER_LEVEL
                                    + "."));

            return 0;
        }

        return showCharacter(context, player);
    }

    private static int showCharacter(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player) {

        return PlayerProgressService.get(player)
                .map(progress -> {

                    context.getSource().sendSuccess(
                            () -> Component.literal(
                                    player.getGameProfile().getName()
                                            + " - Character Level "
                                            + progress.getCharacterLevel()),
                            false);

                    if (progress.classLevels.isEmpty()) {
                        context.getSource().sendSuccess(
                                () -> Component.literal(
                                        "Classes: None"),
                                false);

                        return Command.SINGLE_SUCCESS;
                    }

                    progress.classLevels.entrySet()
                            .stream()
                            .sorted(
                                    Map.Entry.comparingByKey(
                                            Comparator.naturalOrder()))
                            .forEach(entry ->
                                    context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "- "
                                                            + entry.getKey()
                                                            + ": "
                                                            + entry.getValue()),
                                            false));

                    return Command.SINGLE_SUCCESS;
                })
                .orElseGet(() -> {
                    context.getSource().sendFailure(
                            Component.literal(
                                    "Player progression data not found."));
                    return 0;
                });
    }

    private static CompletableFuture<Suggestions> suggestClasses(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {

        RegistryClasses.values().forEach(classId ->
                builder.suggest(classId.getPath()));

        return builder.buildFuture();
    }

    private static int randomizeAbilities(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player) {

        Map<Aptitude, Integer> assignment =
                CharacterInitializationService
                        .generateRandomAssignment();

        if (!CharacterInitializationService
                .initializeStartingAbilities(
                        player,
                        assignment)) {

            context.getSource().sendFailure(
                    Component.literal(
                            "Could not assign starting abilities. "
                                    + "The character must have exactly "
                                    + "one starting class level and "
                                    + "must not have completed ability setup already."));

            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Starting ability scores assigned."),
                false);

        return showAbilities(context, player);
    }

    private static int showAbilities(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player) {

        return PlayerProgressService.get(player)
                .map(progress -> {

                    context.getSource().sendSuccess(
                            () -> Component.literal(
                                    "Starting Abilities: "
                                            + (progress.isStartingAbilitiesAssigned()
                                            ? "Assigned"
                                            : "Not Assigned")),
                            false);

                    for (Aptitude aptitude
                            : CharacterInitializationService.aptitudes()) {

                        int aptitudeLevel =
                                progress.getAptitudeLevel(aptitude);

                        int baseScore =
                                AbilityScoreService.abilityScore(
                                        aptitudeLevel);

                        int effectiveScore =
                                AbilityScoreBonusService.getAbilityScore(
                                        player,
                                        progress,
                                        aptitude);

                        context.getSource().sendSuccess(
                                () -> Component.literal(
                                        aptitude.getName()
                                                + ": "
                                                + baseScore
                                                + " (effective "
                                                + effectiveScore
                                                + ")"),
                                false);
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .orElseGet(() -> {

                    context.getSource().sendFailure(
                            Component.literal(
                                    "Player progression data not found."));

                    return 0;
                });
    }

    private static int resetAbilities(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player) {

        boolean success =
                PlayerProgressService.update(
                        player,
                        progress -> {

                            for (Aptitude aptitude
                                    : CharacterInitializationService
                                    .aptitudes()) {

                                progress.setAptitudeLevel(
                                        aptitude,
                                        1);
                            }

                            progress.setStartingAbilitiesAssigned(false);
                        });

        if (!success) {
            context.getSource().sendFailure(
                    Component.literal(
                            "Could not reset starting abilities."));

            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Starting ability setup reset."),
                false);

        return Command.SINGLE_SUCCESS;
    }
    private static int recommendAbilities(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player) {

        PlayerProgress progress =
                PlayerProgressService.get(player).orElse(null);

        if (progress == null) {
            context.getSource().sendFailure(
                    Component.literal(
                            "Player progression data not found."));
            return 0;
        }

        Map<Aptitude, Integer> assignment =
                CharacterInitializationService
                        .generateRecommendedAssignment(progress);

        if (assignment == null) {
            context.getSource().sendFailure(
                    Component.literal(
                            "Could not determine a valid starting class."));
            return 0;
        }

        if (!CharacterInitializationService
                .initializeStartingAbilities(
                        player,
                        assignment)) {

            context.getSource().sendFailure(
                    Component.literal(
                            "Could not assign recommended starting abilities."));
            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Recommended starting abilities assigned."),
                false);

        return showAbilities(context, player);
    }
    private static int setStartingAbilities(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            int strength,
            int dexterity,
            int constitution,
            int intelligence,
            int wisdom,
            int charisma) {

        Map<Aptitude, Integer> assignment =
                new LinkedHashMap<>();

        assignment.put(
                RegistryAptitudes.STRENGTH,
                strength);

        assignment.put(
                RegistryAptitudes.DEXTERITY,
                dexterity);

        assignment.put(
                RegistryAptitudes.CONSTITUTION,
                constitution);

        assignment.put(
                RegistryAptitudes.INTELLIGENCE,
                intelligence);

        assignment.put(
                RegistryAptitudes.WISDOM,
                wisdom);

        assignment.put(
                RegistryAptitudes.CHARISMA,
                charisma);

        if (!CharacterInitializationService
                .initializeStartingAbilities(
                        player,
                        assignment)) {

            context.getSource().sendFailure(
                    Component.literal(
                            "Invalid starting ability assignment. "
                                    + "You must use each score from "
                                    + "10 through 15 exactly once, "
                                    + "and ability setup must not "
                                    + "already be completed."));

            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Starting abilities assigned."),
                false);

        return showAbilities(context, player);
    }
}