package com.seniors.justlevelingfork.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TitleArgument implements ArgumentType<ResourceLocation> {
    public static final Collection<String> EXAMPLES =
            List.of("justlevelingfork:titleless", "justlevelingfork:administrator", "justlevelingfork:rookie");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_TITLE =
            new DynamicCommandExceptionType(object -> Component.translatable("commands.argument.title.not_found", object));

    public static TitleArgument getArgument() {
        return new TitleArgument();
    }

    public static ResourceLocation getTitle(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return getResource(context.getArgument(name, ResourceLocation.class));
    }

    @Override
    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return getResource(ResourceLocation.read(reader));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        RegistryTitles.defaultIds().forEach(titleId -> builder.suggest(titleId.toString()));
        return builder.buildFuture();
    }

    public static ResourceLocation getResource(ResourceLocation titleId) throws CommandSyntaxException {
        if (RegistryTitles.containsDefaultId(titleId)) {
            return titleId;
        }
        throw ERROR_UNKNOWN_TITLE.create(titleId);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
