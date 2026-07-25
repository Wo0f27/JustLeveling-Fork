package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public final class TitleConfigReloadCommand {
    private TitleConfigReloadCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Runnable reloadAction) {
        dispatcher.register(Commands.literal("titlesreload")
                .requires(source -> source.hasPermission(2))
                .executes(context -> execute(context, reloadAction)));
    }

    private static int execute(CommandContext<CommandSourceStack> context, Runnable reloadAction) {
        reloadAction.run();
        MinecraftServer server = context.getSource().getServer();
        server.getPlayerList().getPlayers().forEach(PlayerProgressService::sync);

        if (context.getSource().getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal("Reloaded title config."));
        }

        return Command.SINGLE_SUCCESS;
    }
}
