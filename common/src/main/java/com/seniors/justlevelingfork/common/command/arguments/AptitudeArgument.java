package com.seniors.justlevelingfork.common.command.arguments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class AptitudeArgument {
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_APTITUDE =
            new DynamicCommandExceptionType(object -> Component.translatable("commands.argument.aptitude.not_found", object));

    private AptitudeArgument() {
    }

    public static StringArgumentType getArgument() {
        return StringArgumentType.word();
    }

    public static String getAptitude(CommandContext<CommandSourceStack> context, String name)
            throws CommandSyntaxException {
        String aptitudeName = StringArgumentType.getString(context, name);
        if (RegistryAptitudes.getAptitude(aptitudeName) == null) {
            throw ERROR_UNKNOWN_APTITUDE.create(aptitudeName);
        }
        return aptitudeName.toLowerCase(Locale.ROOT);
    }

    public static CompletableFuture<Suggestions> listSuggestions(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        RegistryAptitudes.values().stream()
                .map(aptitude -> capitalize(aptitude.getName()))
                .filter(name -> remaining.isEmpty() || name.toLowerCase(Locale.ROOT).contains(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
