package com.seniors.justlevelingfork.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seniors.justlevelingfork.client.screen.AptitudesOverviewScreen;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.RegistryItems;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClientTabs {
    private static final Minecraft CLIENT = Minecraft.getInstance();
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int TAB_SPACING = 27;
    private static BooleanSupplier hidden = () -> false;

    private ClientTabs() {
    }

    public static void render(GuiGraphics graphics, Screen currentScreen, int mouseX, int mouseY, int textureWidth, int textureHeight) {
        if (CLIENT.player == null || hidden.getAsBoolean()) {
            return;
        }

        int left = (CLIENT.getWindow().getGuiScaledWidth() - textureWidth) / 2;
        int top = (CLIENT.getWindow().getGuiScaledHeight() - textureHeight) / 2 - 28;
        List<Tab> tabs = tabs(currentScreen);
        for (int i = 0; i < tabs.size(); i++) {
            renderTab(graphics, tabs.get(i), left + i * TAB_SPACING, top, mouseX, mouseY);
        }
    }

    public static boolean mouseClicked(Screen currentScreen, double mouseX, double mouseY, int button, int textureWidth, int textureHeight) {
        if (CLIENT.player == null || hidden.getAsBoolean() || button != 0) {
            return false;
        }

        int left = (CLIENT.getWindow().getGuiScaledWidth() - textureWidth) / 2;
        int top = (CLIENT.getWindow().getGuiScaledHeight() - textureHeight) / 2 - 28;
        List<Tab> tabs = tabs(currentScreen);
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (!tab.selected && isMouseWithin(left + i * TAB_SPACING, top, mouseX, mouseY, TAB_WIDTH, TAB_HEIGHT)) {
                CLIENT.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                CLIENT.setScreen(tab.createScreen());
                return true;
            }
        }

        return false;
    }

    public static void setHiddenSupplier(BooleanSupplier hidden) {
        ClientTabs.hidden = Optional.ofNullable(hidden).orElse(() -> false);
    }

    private static List<Tab> tabs(Screen currentScreen) {
        return List.of(
                new Tab(
                        "inventory",
                        playerHead(),
                        Component.translatable("container.inventory"),
                        currentScreen instanceof InventoryScreen,
                        () -> new InventoryScreen(CLIENT.player)),
                new Tab(
                        "leveling",
                        levelingBook(),
                        Component.translatable("screen.aptitude.title"),
                        currentScreen instanceof AptitudesOverviewScreen,
                        AptitudesOverviewScreen::new));
    }

    private static void renderTab(GuiGraphics graphics, Tab tab, int x, int y, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        RenderSystem.enableBlend();
        graphics.blit(HandlerResources.TABS, x, y, tab.name.equals("inventory") ? 0 : 26, tab.selected ? 32 : 0, TAB_WIDTH, TAB_HEIGHT);
        float scale = tab.icon.getItem() instanceof net.minecraft.world.item.StandingAndWallBlockItem ? 1.125F : 1.0F;
        float iconX = (x + 13.0F - 8.0F) / scale;
        float iconY = (y + 15.0F - 8.0F + (tab.selected ? 0.0F : 2.0F)) / scale;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.renderItem(tab.icon, (int) iconX, (int) iconY);
        graphics.pose().popPose();
        graphics.pose().popPose();

        if (isMouseWithin(x, y, mouseX, mouseY, TAB_WIDTH, TAB_HEIGHT)) {
            graphics.renderTooltip(CLIENT.font, tab.title, mouseX, mouseY);
        }
    }

    private static ItemStack levelingBook() {
        Item item = BuiltInRegistries.ITEM.get(RegistryItems.id("leveling_book"));
        return new ItemStack(item == Items.AIR ? Items.BOOK : item);
    }

    private static ItemStack playerHead() {
        return new ItemStack(Items.PLAYER_HEAD);
    }

    private static boolean isMouseWithin(int x, int y, double mouseX, double mouseY, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private record Tab(String name, ItemStack icon, Component title, boolean selected, ScreenFactory screenFactory) {
        private Screen createScreen() {
            return screenFactory.create();
        }
    }

    @FunctionalInterface
    private interface ScreenFactory {
        Screen create();
    }
}
