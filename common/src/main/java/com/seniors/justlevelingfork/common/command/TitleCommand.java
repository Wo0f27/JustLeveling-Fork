package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class TitleCommand {
    private TitleCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("titles")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("title", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    RegistryTitles.defaultIds().forEach(titleId -> builder.suggest(titleId.toString()));
                                    return builder.buildFuture();
                                })
                                .then(Commands.literal("set")
                                        .then(Commands.literal("true")
                                                .executes(context -> setTitle(context, true)))
                                        .then(Commands.literal("false")
                                                .executes(context -> setTitle(context, false)))))));
    }

    private static int setTitle(CommandContext<CommandSourceStack> context, boolean unlocked)
            throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation titleId = ResourceLocationArgument.getId(context, "title");
        Title title = RegistryTitles.getTitle(titleId.toString());
        if (title == null || !PlayerProgressService.setUnlockTitle(player, title, unlocked)) {
            context.getSource().sendFailure(Component.translatable("commands.message.capability.not_found"));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable(
                unlocked ? "commands.message.title.set" : "commands.message.title.unset",
                player.getName().copy().withStyle(ChatFormatting.BOLD),
                Component.translatable(title.getKey()).withStyle(ChatFormatting.BOLD)), false);
        return 1;
    }
}
