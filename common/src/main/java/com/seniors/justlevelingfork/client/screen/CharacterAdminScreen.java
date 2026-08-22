package com.seniors.justlevelingfork.client.screen;

import com.seniors.justlevelingfork.common.player.CharacterAdminClientState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CharacterAdminScreen extends Screen {

    private static final int WIDTH = 208;
    private static final int HEIGHT = 150;

    private static final int FONT_COLOR =
            0x3E3E3E;

    private static final int WHITE =
            0xF0F0F0;

    private static final int GRAY =
            0xAAAAAA;

    private final Screen parent;

    public CharacterAdminScreen(
            Screen parent) {

        super(Component.literal(
                "JLF Character Admin"));

        this.parent = parent;
    }

    @Override
    public void tick() {

        /*
         * If the server later reports that access
         * is no longer allowed, leave the screen.
         */
        if (!CharacterAdminClientState
                .isAllowed()) {

            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick) {

        renderBackground(graphics);

        int left = left();
        int top = top();

        graphics.fill(
                left,
                top,
                left + WIDTH,
                top + HEIGHT,
                0xFF202020);

        graphics.fill(
                left + 3,
                top + 3,
                left + WIDTH - 3,
                top + HEIGHT - 3,
                0xFFD0D0D0);

        drawCentered(
                graphics,
                Component.literal(
                                "JLF CHARACTER ADMIN")
                        .withStyle(
                                ChatFormatting.BOLD),
                left + WIDTH / 2,
                top + 12,
                FONT_COLOR);

        drawCentered(
                graphics,
                Component.literal(
                                "Operator Testing Tools")
                        .withStyle(
                                ChatFormatting.DARK_GRAY),
                left + WIDTH / 2,
                top + 28,
                FONT_COLOR);

        drawCentered(
                graphics,
                Component.literal(
                                "Admin controls will be added")
                        .withStyle(
                                ChatFormatting.GRAY),
                left + WIDTH / 2,
                top + 58,
                FONT_COLOR);

        drawCentered(
                graphics,
                Component.literal(
                                "in the next checkpoint.")
                        .withStyle(
                                ChatFormatting.GRAY),
                left + WIDTH / 2,
                top + 70,
                FONT_COLOR);

        renderBackButton(
                graphics,
                left,
                top,
                mouseX,
                mouseY);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick);
    }

    private void renderBackButton(
            GuiGraphics graphics,
            int left,
            int top,
            int mouseX,
            int mouseY) {

        int x =
                left + WIDTH / 2 - 30;

        int y =
                top + HEIGHT - 24;

        boolean hover =
                isMouseWithin(
                        x,
                        y,
                        mouseX,
                        mouseY,
                        60,
                        14);

        graphics.fill(
                x,
                y,
                x + 60,
                y + 14,
                hover
                        ? 0xFF777777
                        : 0xFF555555);

        graphics.fill(
                x + 1,
                y + 1,
                x + 59,
                y + 13,
                hover
                        ? 0xFF484848
                        : 0xFF353535);

        Component text =
                Component.literal("< Back");

        graphics.drawString(
                font,
                text,
                x + 30
                        - font.width(text) / 2,
                y + 3,
                hover
                        ? WHITE
                        : GRAY,
                false);
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button) {

        if (button != 0) {
            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button);
        }

        int left = left();
        int top = top();

        if (isMouseWithin(
                left + WIDTH / 2 - 30,
                top + HEIGHT - 24,
                mouseX,
                mouseY,
                60,
                14)) {

            minecraft.setScreen(parent);
            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void drawCentered(
            GuiGraphics graphics,
            Component component,
            int x,
            int y,
            int color) {

        graphics.drawString(
                font,
                component,
                x - font.width(component) / 2,
                y,
                color,
                false);
    }

    private int left() {
        return (width - WIDTH) / 2;
    }

    private int top() {
        return (height - HEIGHT) / 2;
    }

    private static boolean isMouseWithin(
            int x,
            int y,
            double mouseX,
            double mouseY,
            int width,
            int height) {

        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}