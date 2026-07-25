package com.seniors.justlevelingfork.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;

public class AptitudeArgument implements ArgumentType<String> {
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_APTITUDE =
            new DynamicCommandExceptionType(object -> Component.translatable("commands.argument.aptitude.not_found", object));

    public static AptitudeArgument getArgument() {
        return new AptitudeArgument();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readString();
        if (RegistryAptitudes.getAptitude(name) == null) {
            throw ERROR_UNKNOWN_APTITUDE.create(name);
        }
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        RegistryAptitudes.values().stream()
                .map(aptitude -> capitalize(aptitude.getName()))
                .filter(name -> remaining.isEmpty() || name.toLowerCase(Locale.ROOT).contains(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return RegistryAptitudes.values().stream().map(aptitude -> capitalize(aptitude.getName())).toList();
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
