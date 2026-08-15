package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.common.command.RegisterItemCommand;
import com.seniors.justlevelingfork.common.command.ConfigLimitCommands;
import com.seniors.justlevelingfork.common.command.AptitudeLevelCommand;
import com.seniors.justlevelingfork.common.command.AptitudesReloadCommand;
import com.seniors.justlevelingfork.common.command.TitleCommand;
import com.seniors.justlevelingfork.common.command.TitleConfigReloadCommand;
import com.seniors.justlevelingfork.common.event.BlockBreakSkillEffects;
import com.seniors.justlevelingfork.common.event.InteractionRestrictions;
import com.seniors.justlevelingfork.common.player.PlayerTickEffects;
import com.seniors.justlevelingfork.config.ForgeLockItemStore;
import com.seniors.justlevelingfork.config.ForgeCommonConfig;
import com.seniors.justlevelingfork.config.ForgeTitleModelStore;
import com.seniors.justlevelingfork.integration.ForgeCuriosIntegration;
import com.seniors.justlevelingfork.integration.ForgeModularItemRestrictions;
import com.seniors.justlevelingfork.network.ForgeServerNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import com.seniors.justlevelingfork.common.command.CharacterCommand;
import com.seniors.justlevelingfork.common.feat.FeatManager;
import net.minecraftforge.event.AddReloadListenerEvent;

public final class ForgeRegistryCommonEvents {
    private ForgeRegistryCommonEvents() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(ForgeRegistryCommonEvents.class);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RegisterItemCommand.register(
                event.getDispatcher(),
                ForgeLockItemStore.instance(),
                source -> source.getServer().getPlayerList().getPlayers().forEach(player ->
                        ForgeServerNetworking.syncLockItems(player, ForgeLockItemStore.instance().lockItems())));
        AptitudesReloadCommand.register(
                event.getDispatcher(),
                ForgeLockItemStore.instance()::reload,
                player -> ForgeServerNetworking.syncLockItems(player, ForgeLockItemStore.instance().lockItems()));
        AptitudeLevelCommand.register(event.getDispatcher());
        CharacterCommand.register(event.getDispatcher());
        TitleCommand.register(event.getDispatcher());
        TitleConfigReloadCommand.register(
                event.getDispatcher(),
                ForgeTitleModelStore::reload,
                ForgeServerNetworking::syncTitleDefinitions);
        ConfigLimitCommands.register(event.getDispatcher(), new ConfigLimitCommands.ConfigLimitStore() {
            @Override
            public void setAptitudeMaxLevel(int level) {
                ForgeCommonConfig.setAptitudeMaxLevel(level);
            }

            @Override
            public void setPlayersMaxGlobalLevel(int level) {
                ForgeCommonConfig.setPlayersMaxGlobalLevel(level);
            }

            @Override
            public void sync(net.minecraft.commands.CommandSourceStack source) {
                source.getServer().getPlayerList().getPlayers().forEach(ForgeServerNetworking::syncCommonConfig);
            }
        });
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {event.addListener(FeatManager.INSTANCE);}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer serverPlayer) {
            PlayerTickEffects.apply(serverPlayer);
            if (ModList.get().isLoaded("curios")) {
                ForgeCuriosIntegration.dropLockedAccessories(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer && !(serverPlayer instanceof FakePlayer)) {
            BlockBreakSkillEffects.afterBlockBreak(
                    serverPlayer.level(), serverPlayer, event.getPos(), event.getState());
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!InteractionRestrictions.canUseItem(event.getEntity(), event.getItemStack())
                || (event.getEntity() instanceof ServerPlayer serverPlayer
                && !ForgeModularItemRestrictions.canUse(serverPlayer, event.getItemStack()))) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!InteractionRestrictions.canUseBlock(
                event.getEntity(),
                event.getLevel().getBlockState(event.getPos()).getBlock(),
                event.getItemStack())
                || (event.getEntity() instanceof ServerPlayer serverPlayer
                && !ForgeModularItemRestrictions.canUse(serverPlayer, event.getItemStack()))) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!InteractionRestrictions.canAttackBlock(
                event.getEntity(),
                event.getLevel().getBlockState(event.getPos()).getBlock(),
                event.getItemStack())
                || (event.getEntity() instanceof ServerPlayer serverPlayer
                && !ForgeModularItemRestrictions.canUse(serverPlayer, event.getItemStack()))) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!InteractionRestrictions.canUseEntity(event.getEntity(), event.getTarget(), event.getItemStack())
                || (event.getEntity() instanceof ServerPlayer serverPlayer
                && !ForgeModularItemRestrictions.canUse(serverPlayer, event.getItemStack()))) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!InteractionRestrictions.canUseEntity(event.getEntity(), event.getTarget(), event.getItemStack())
                || (event.getEntity() instanceof ServerPlayer serverPlayer
                && !ForgeModularItemRestrictions.canUse(serverPlayer, event.getItemStack()))) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!InteractionRestrictions.canAttackEntity(
                event.getEntity(), event.getTarget(), event.getEntity().getMainHandItem())
                || (event.getEntity() instanceof ServerPlayer serverPlayer
                && !ForgeModularItemRestrictions.canUse(serverPlayer, event.getEntity().getMainHandItem()))) {
            event.setCanceled(true);
        }
    }
}
