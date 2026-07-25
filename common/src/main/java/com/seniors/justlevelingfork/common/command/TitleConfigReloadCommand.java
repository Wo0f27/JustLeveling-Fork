package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.function.Consumer;

public final class TitleConfigReloadCommand {
    private TitleConfigReloadCommand() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            Runnable reloadAction,
            Consumer<ServerPlayer> titleDefinitionsSync) {
        dispatcher.register(Commands.literal("titlesreload")
                .requires(source -> source.hasPermission(2))
                .executes(context -> execute(context, reloadAction, titleDefinitionsSync)));
    }

    private static int execute(
            CommandContext<CommandSourceStack> context,
            Runnable reloadAction,
            Consumer<ServerPlayer> titleDefinitionsSync) {
        reloadAction.run();
        // An integrated client shares this registry with its server. Rebuild from
        // the freshly loaded server config before evaluating and syncing titles.
        RegistryTitles.clearClientTitleModels();
        MinecraftServer server = context.getSource().getServer();
        server.getPlayerList().getPlayers().forEach(player -> {
            titleDefinitionsSync.accept(player);
            PlayerProgressService.sync(player);
        });

        if (context.getSource().getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal("Reloaded title config."));
        }

        return Command.SINGLE_SUCCESS;
    }
}
