package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.player.ForgePlayerProgressProvider;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.config.ForgeLockItemStore;
import com.seniors.justlevelingfork.network.ForgeServerNetworking;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ForgeRegistryCapabilities {
    public static final Capability<PlayerProgress> PLAYER_PROGRESS =
            CapabilityManager.get(new CapabilityToken<>() {});

    private static final ResourceLocation PLAYER_PROGRESS_ID =
            new ResourceLocation(Constants.MOD_ID, "player_progress");

    private ForgeRegistryCapabilities() {
    }

    public static void load() {
        PlayerProgressService.setProgressProvider(player ->
                player.getCapability(PLAYER_PROGRESS).resolve());
        PlayerProgressService.setChangeListener((player, progress) -> {});
        PlayerProgressService.setSyncHandler(ForgeServerNetworking::syncPlayerProgress);
        MinecraftForge.EVENT_BUS.register(ForgeRegistryCapabilities.class);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerProgress.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(PLAYER_PROGRESS_ID, new ForgePlayerProgressProvider());
        }
    }

    @SubscribeEvent
    public static void copyOnClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        Optional<PlayerProgress> oldProgress = event.getOriginal().getCapability(PLAYER_PROGRESS).resolve();
        Optional<PlayerProgress> newProgress = event.getEntity().getCapability(PLAYER_PROGRESS).resolve();
        oldProgress.ifPresent(oldValue -> newProgress.ifPresent(newValue -> {
            newValue.copyFrom(oldValue);
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PlayerProgressService.refreshPassiveModifiers(serverPlayer);
                PlayerProgressService.sync(serverPlayer);
            }
        }));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void refreshOnLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerProgressService.refreshPassiveModifiers(serverPlayer);
            PlayerProgressService.sync(serverPlayer);
            ForgeServerNetworking.syncLockItems(serverPlayer, ForgeLockItemStore.instance().lockItems());
            ForgeServerNetworking.syncCommonConfig(serverPlayer);
        }
    }
}
