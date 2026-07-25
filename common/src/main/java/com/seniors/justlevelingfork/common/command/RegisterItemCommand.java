package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RegisterItemCommand {
    private RegisterItemCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LockItemStore lockItemStore) {
        dispatcher.register(Commands.literal("registeritem")
                .requires(source -> source.hasPermission(2))
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
                        .then(Commands.argument("level", IntegerArgumentType.integer())
                                .executes(context -> execute(context, lockItemStore)))));
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private static int execute(CommandContext<CommandSourceStack> context, LockItemStore lockItemStore)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            player.sendSystemMessage(Component.literal("No item detected in main hand!"));
            return Command.SINGLE_SUCCESS;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String aptitudeName = context.getArgument("aptitude", String.class);
        int level = IntegerArgumentType.getInteger(context, "level");
        LockItemRegistration.Result result =
                LockItemRegistration.apply(lockItemStore.lockItems(), itemId, aptitudeName, level);

        if (result.changed()) {
            lockItemStore.save();
        }

        player.sendSystemMessage(Component.literal(result.message()));
        return Command.SINGLE_SUCCESS;
    }

    public interface LockItemStore {
        List<LockItem> lockItems();

        void save();
    }
}
