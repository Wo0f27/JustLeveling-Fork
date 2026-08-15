package com.seniors.justlevelingfork.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seniors.justlevelingfork.common.player.ClassProgressionService;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClassLevelUpScreen extends Screen {

    /*
     * Slightly wider than the normal JLF aptitude card.
     *
     * The normal 176px card is too narrow for two class columns
     * containing names such as "Barbarian" plus level information.
     */
    private static final int WIDTH = 208;
    private static final int HEIGHT = 180;

    private static final int ROW_WIDTH = 90;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_SPACING = 18;

    private static final int FONT_COLOR = 0x3E3E3E;
    private static final int WHITE = 0xF0F0F0;
    private static final int GREEN = 0x55DD55;
    private static final int GRAY = 0xAAAAAA;

    private final Screen parent;

    public ClassLevelUpScreen(Screen parent) {
        super(Component.literal("Choose Class"));
        this.parent = parent;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick) {

        renderBackground(graphics);
        RenderSystem.enableBlend();

        int left = left();
        int top = top();

        /*
         * Custom JLF-style panel.
         *
         * We deliberately do NOT use SKILL_PAGE[0] here because
         * that texture contains baked aptitude boxes.
         */
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

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        drawCentered(
                graphics,
                Component.literal("Choose Class Level")
                        .withStyle(ChatFormatting.BOLD),
                left + WIDTH / 2,
                top + 9,
                FONT_COLOR);

        if (progress == null) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Progression data unavailable"),
                    left + WIDTH / 2,
                    top + 30,
                    FONT_COLOR);

            super.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick);

            return;
        }

        int currentLevel =
                progress.getCharacterLevel();

        int nextLevel =
                Math.min(
                        currentLevel + 1,
                        ClassProgressionService.MAX_CHARACTER_LEVEL);

        /*
         * Plain ASCII arrow.
         *
         * This avoids the previous:
         * â†’
         * encoding problem.
         */
        drawCentered(
                graphics,
                Component.literal(
                        "Character Level "
                                + currentLevel
                                + " -> "
                                + nextLevel),
                left + WIDTH / 2,
                top + 22,
                FONT_COLOR);

        List<ResourceLocation> classes =
                RegistryClasses.values();

        for (int i = 0; i < classes.size(); i++) {

            ResourceLocation classId =
                    classes.get(i);

            int column =
                    i % 2;

            int row =
                    i / 2;

            int x =
                    left + 11
                            + column * 96;

            int y =
                    top + 42
                            + row * ROW_SPACING;

            renderClassEntry(
                    graphics,
                    progress,
                    classId,
                    x,
                    y,
                    mouseX,
                    mouseY);
        }

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

    private void renderClassEntry(
            GuiGraphics graphics,
            PlayerProgress progress,
            ResourceLocation classId,
            int x,
            int y,
            int mouseX,
            int mouseY) {

        boolean hover =
                isMouseWithin(
                        x,
                        y,
                        mouseX,
                        mouseY,
                        ROW_WIDTH,
                        ROW_HEIGHT);

        int currentClassLevel =
                progress.getClassLevel(
                        classId.toString());

        boolean existingClass =
                currentClassLevel > 0;

        /*
         * Dark JLF-style class button.
         */
        int borderColor =
                hover
                        ? 0xFF777777
                        : 0xFF555555;

        int backgroundColor =
                hover
                        ? 0xFF484848
                        : 0xFF353535;

        graphics.fill(
                x,
                y,
                x + ROW_WIDTH,
                y + ROW_HEIGHT,
                borderColor);

        graphics.fill(
                x + 1,
                y + 1,
                x + ROW_WIDTH - 1,
                y + ROW_HEIGHT - 1,
                backgroundColor);

        String className =
                displayName(classId);

        int nameColor =
                existingClass
                        ? GREEN
                        : WHITE;

        graphics.drawString(
                font,
                className,
                x + 4,
                y + 4,
                nameColor,
                false);

        /*
         * Compact version instead of "0 -> 1".
         *
         * This keeps even Barbarian from colliding
         * with the level indicator.
         */
        String levelText =
                currentClassLevel
                        + ">"
                        + (currentClassLevel + 1);

        graphics.drawString(
                font,
                levelText,
                x + ROW_WIDTH
                        - 4
                        - font.width(levelText),
                y + 4,
                GRAY,
                false);

        if (!hover) {
            return;
        }

        if (existingClass) {

            graphics.renderTooltip(
                    font,
                    Component.literal(
                                    className
                                            + " "
                                            + currentClassLevel
                                            + " -> "
                                            + (currentClassLevel + 1))
                            .withStyle(
                                    ChatFormatting.GREEN),
                    mouseX,
                    mouseY);

        } else {

            graphics.renderTooltip(
                    font,
                    Component.literal(
                                    "Multiclass into "
                                            + className
                                            + " (0 -> 1)")
                            .withStyle(
                                    ChatFormatting.GOLD),
                    mouseX,
                    mouseY);
        }
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
                top + 158;

        boolean hover =
                isMouseWithin(
                        x,
                        y,
                        mouseX,
                        mouseY,
                        60,
                        14);

        int borderColor =
                hover
                        ? 0xFF777777
                        : 0xFF555555;

        int backgroundColor =
                hover
                        ? 0xFF484848
                        : 0xFF353535;

        graphics.fill(
                x,
                y,
                x + 60,
                y + 14,
                borderColor);

        graphics.fill(
                x + 1,
                y + 1,
                x + 59,
                y + 13,
                backgroundColor);

        Component back =
                Component.literal("< Back");

        graphics.drawString(
                font,
                back,
                x + 30 - font.width(back) / 2,
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

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        if (progress == null) {
            return false;
        }

        int left = left();
        int top = top();

        /*
         * Back button.
         */
        if (isMouseWithin(
                left + WIDTH / 2 - 30,
                top + 158,
                mouseX,
                mouseY,
                60,
                14)) {

            minecraft.setScreen(parent);
            return true;
        }

        /*
         * If the level-up disappeared while the screen
         * was open, return safely.
         */
        if (progress.getPendingLevelUps() <= 0) {

            minecraft.setScreen(parent);
            return true;
        }

        List<ResourceLocation> classes =
                RegistryClasses.values();

        for (int i = 0; i < classes.size(); i++) {

            int column =
                    i % 2;

            int row =
                    i / 2;

            int x =
                    left + 11
                            + column * 96;

            int y =
                    top + 42
                            + row * ROW_SPACING;

            if (!isMouseWithin(
                    x,
                    y,
                    mouseX,
                    mouseY,
                    ROW_WIDTH,
                    ROW_HEIGHT)) {

                continue;
            }

            ResourceLocation classId =
                    classes.get(i);

            /*
             * Client only requests the level.
             *
             * Server remains authoritative and validates:
             * - pending level-up
             * - character level cap
             * - multiclass prerequisites
             */
            PlayerProgressClientRequests
                    .requestClassLevelUp(classId);

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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String displayName(
            ResourceLocation classId) {

        String path =
                classId.getPath();

        if (path.isEmpty()) {
            return path;
        }

        String normalized =
                path.replace('_', ' ');

        return normalized.substring(0, 1)
                .toUpperCase(Locale.ROOT)
                + normalized.substring(1);
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
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }
}