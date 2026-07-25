package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.function.Consumer;

public final class AptitudesReloadCommand {
    private AptitudesReloadCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Runnable reloadAction) {
        register(dispatcher, reloadAction, player -> {});
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            Runnable reloadAction,
            Consumer<ServerPlayer> syncAction) {
        dispatcher.register(Commands.literal("aptitudesreload")
                .requires(source -> source.hasPermission(2))
                .executes(context -> execute(context, reloadAction, syncAction)));
    }

    private static int execute(
            CommandContext<CommandSourceStack> context,
            Runnable reloadAction,
            Consumer<ServerPlayer> syncAction) {
        reloadAction.run();
        context.getSource().getServer().getPlayerList().getPlayers().forEach(syncAction);

        if (context.getSource().getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal("Forcing refresh of aptitudes..."));
        }

        return Command.SINGLE_SUCCESS;
    }
}
