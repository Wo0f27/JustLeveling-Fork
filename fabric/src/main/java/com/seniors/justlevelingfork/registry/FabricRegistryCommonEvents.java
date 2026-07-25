package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.common.command.RegisterItemCommand;
import com.seniors.justlevelingfork.common.command.ConfigLimitCommands;
import com.seniors.justlevelingfork.common.command.AptitudeLevelCommand;
import com.seniors.justlevelingfork.common.command.AptitudesReloadCommand;
import com.seniors.justlevelingfork.common.command.TitleCommand;
import com.seniors.justlevelingfork.common.command.TitleConfigReloadCommand;
import com.seniors.justlevelingfork.common.event.BlockBreakSkillEffects;
import com.seniors.justlevelingfork.common.event.InteractionRestrictions;
import com.seniors.justlevelingfork.common.player.FabricPlayerProgressStore;
import com.seniors.justlevelingfork.common.player.PlayerTickEffects;
import com.seniors.justlevelingfork.config.FabricLockItemStore;
import com.seniors.justlevelingfork.config.FabricCommonConfig;
import com.seniors.justlevelingfork.config.FabricTitleModelStore;
import com.seniors.justlevelingfork.integration.FabricTrinketsIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.server.level.ServerPlayer;

public final class FabricRegistryCommonEvents {
    private FabricRegistryCommonEvents() {
    }

    public static void load() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                {
                    RegisterItemCommand.register(dispatcher, FabricLockItemStore.instance());
                    AptitudesReloadCommand.register(
                            dispatcher,
                            FabricLockItemStore.instance()::reload,
                            FabricPlayerProgressStore::syncLockItems);
                    AptitudeLevelCommand.register(dispatcher);
                    TitleCommand.register(dispatcher);
                    TitleConfigReloadCommand.register(dispatcher, FabricTitleModelStore::reload);
                    ConfigLimitCommands.register(dispatcher, new ConfigLimitCommands.ConfigLimitStore() {
                        @Override
                        public void setAptitudeMaxLevel(int level) {
                            FabricCommonConfig.setAptitudeMaxLevel(level);
                        }

                        @Override
                        public void setPlayersMaxGlobalLevel(int level) {
                            FabricCommonConfig.setPlayersMaxGlobalLevel(level);
                        }
                    });
                });
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(player -> {
                    PlayerTickEffects.apply(player);
                    if (FabricLoader.getInstance().isModLoaded("trinkets")) {
                        FabricTrinketsIntegration.dropLockedAccessories(player);
                    }
                }));
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                BlockBreakSkillEffects.afterBlockBreak(world, serverPlayer, pos, state);
            }
        });
        UseItemCallback.EVENT.register((player, world, hand) -> InteractionRestrictions.canUseItem(
                        player, player.getItemInHand(hand))
                ? InteractionResultHolder.pass(player.getItemInHand(hand))
                : InteractionResultHolder.fail(player.getItemInHand(hand)));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> InteractionRestrictions.canUseBlock(
                        player, world.getBlockState(hitResult.getBlockPos()).getBlock(), player.getItemInHand(hand))
                ? InteractionResult.PASS
                : InteractionResult.FAIL);
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> InteractionRestrictions.canAttackBlock(
                        player, world.getBlockState(pos).getBlock(), player.getItemInHand(hand))
                ? InteractionResult.PASS
                : InteractionResult.FAIL);
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> InteractionRestrictions.canUseEntity(
                        player, entity, player.getItemInHand(hand))
                ? InteractionResult.PASS
                : InteractionResult.FAIL);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> InteractionRestrictions.canAttackEntity(
                        player, entity, player.getItemInHand(hand))
                ? InteractionResult.PASS
                : InteractionResult.FAIL);
    }
}
