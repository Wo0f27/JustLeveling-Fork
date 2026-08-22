package com.seniors.justlevelingfork.client;

import com.seniors.justlevelingfork.client.ClientKeyMappings;
import com.seniors.justlevelingfork.client.gui.ClientOverlayState;
import com.seniors.justlevelingfork.client.gui.ClientTabs;
import com.seniors.justlevelingfork.client.screen.CommonConfigScreen;
import com.seniors.justlevelingfork.config.ForgeCommonConfig;
import com.seniors.justlevelingfork.integration.L2TabsIntegration;
import com.seniors.justlevelingfork.registry.ForgeRegistryItems;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import dev.xkmc.l2tabs.tabs.core.TabRegistry;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import com.seniors.justlevelingfork.common.player.CharacterAdminClientState;

public final class ForgeClientEvents {
    private ForgeClientEvents() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(ForgeClientEvents.class);
    }

    public static void loadModBus(IEventBus modEventBus) {
        modEventBus.addListener(ForgeClientEvents::registerKeyMappings);
        modEventBus.addListener(ForgeClientEvents::clientSetup);
        registerConfigScreen();
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        if (L2TabsIntegration.isModLoaded()) {
            ClientTabs.setHiddenSupplier(L2TabsIntegration::isModLoaded);
            event.enqueueWork(() -> TabRegistry.registerTab(
                    3500,
                    TabJustLeveling::new,
                    ForgeRegistryItems.LEVELING_BOOK::get,
                    Component.literal("Aptitudes")));
        }
    }

    private static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new CommonConfigScreen(parent, new ForgeConfigSaver())));
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientKeyMappings.OPEN_APTITUDES);
    }

    @SubscribeEvent
    public static void onHudRender(CustomizeGuiOverlayEvent.DebugText event) {
        ClientOverlayState.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientOverlayState.tick();
            ClientKeyMappings.handleClientTick(net.minecraft.client.Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        RegistryTitles.clearClientTitleModels();
        CharacterAdminClientState.clear();
    }

    private static final class ForgeConfigSaver implements CommonConfigScreen.ConfigSaver {
        @Override
        public void setAptitudeMaxLevel(int value) {
            ForgeCommonConfig.setAptitudeMaxLevel(value);
        }

        @Override
        public void setPlayersMaxGlobalLevel(int value) {
            ForgeCommonConfig.setPlayersMaxGlobalLevel(value);
        }

        @Override
        public void setAptitudeFirstCostLevel(int value) {
            ForgeCommonConfig.setAptitudeFirstCostLevel(value);
        }

        @Override
        public void setShowPotionsHud(boolean value) {
            ForgeCommonConfig.setShowPotionsHud(value);
        }

        @Override
        public void setShowLuckyDropSkillOverlay(boolean value) {
            ForgeCommonConfig.setShowLuckyDropSkillOverlay(value);
        }

        @Override
        public void setShowCriticalRollSkillOverlay(boolean value) {
            ForgeCommonConfig.setShowCriticalRollSkillOverlay(value);
        }

        @Override
        public void setShowSkillModName(boolean value) {
            ForgeCommonConfig.setShowSkillModName(value);
        }

        @Override
        public void setShowTitleModName(boolean value) {
            ForgeCommonConfig.setShowTitleModName(value);
        }

        @Override
        public void setPassiveSort(com.seniors.justlevelingfork.client.core.SortPassives value) {
            ForgeCommonConfig.setPassiveSort(value);
        }

        @Override
        public void setSkillSort(com.seniors.justlevelingfork.client.core.SortSkills value) {
            ForgeCommonConfig.setSkillSort(value);
        }

        @Override
        public void setDropLockedItems(boolean value) {
            ForgeCommonConfig.setDropLockedItems(value);
        }

        @Override
        public void setAllowLockedItemsInInventory(boolean value) {
            ForgeCommonConfig.setAllowLockedItemsInInventory(value);
        }

        @Override
        public void setHideMetUsageRequirements(boolean value) {
            ForgeCommonConfig.setHideMetUsageRequirements(value);
        }

        @Override
        public void setSkillResetRefundsSpentLevels(boolean value) {
            ForgeCommonConfig.setSkillResetRefundsSpentLevels(value);
        }
    }
}
