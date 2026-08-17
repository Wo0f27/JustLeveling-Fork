package com.seniors.justlevelingfork.client.screen;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.network.FeatDefinitionsSyncPayload;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AbilityScoreSelectionScreen extends Screen {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 150;

    private static final int BUTTON_WIDTH = 82;
    private static final int BUTTON_HEIGHT = 22;

    private static final int WHITE = 0xF0F0F0;
    private static final int GRAY = 0xAAAAAA;
    private static final int GREEN = 0x70E050;
    private static final int TEXT = 0x303030;

    private final Screen parent;
    private final FeatDefinitionsSyncPayload.Definition feat;

    public AbilityScoreSelectionScreen(
            Screen parent,
            FeatDefinitionsSyncPayload.Definition feat) {

        super(
                Component.literal(
                        "Choose Ability"));

        this.parent = parent;
        this.feat = feat;
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
                                feat.name())
                        .withStyle(
                                ChatFormatting.BOLD),
                left + WIDTH / 2,
                top + 9,
                TEXT);

        drawCentered(
                graphics,
                Component.literal(
                        "Choose an ability to increase"),
                left + WIDTH / 2,
                top + 22,
                TEXT);

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        if (progress == null) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Progression data unavailable"),
                    left + WIDTH / 2,
                    top + 50,
                    TEXT);

            super.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick);

            return;
        }

        List<Aptitude> aptitudes =
                List.of(
                        RegistryAptitudes.STRENGTH,
                        RegistryAptitudes.DEXTERITY,
                        RegistryAptitudes.CONSTITUTION,
                        RegistryAptitudes.INTELLIGENCE,
                        RegistryAptitudes.WISDOM,
                        RegistryAptitudes.CHARISMA);

        for (int i = 0;
             i < aptitudes.size();
             i++) {

            int column =
                    i % 2;

            int row =
                    i / 2;

            int x =
                    left + 13
                            + column * 92;

            int y =
                    top + 42
                            + row * 28;

            renderAbilityButton(
                    graphics,
                    progress,
                    aptitudes.get(i),
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

    private void renderAbilityButton(
            GuiGraphics graphics,
            PlayerProgress progress,
            Aptitude aptitude,
            int x,
            int y,
            int mouseX,
            int mouseY) {

        int current =
                progress.getAptitudeLevel(
                        aptitude);

        boolean atMaximum =
                current
                        >= CommonConfigService
                        .aptitudeMaxLevel();

        boolean hover =
                isMouseWithin(
                        x,
                        y,
                        mouseX,
                        mouseY,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT);

        int border =
                atMaximum
                        ? 0xFF555555
                        : hover
                        ? 0xFF88AA66
                        : 0xFF5F7750;

        int background =
                hover
                        ? 0xFF484848
                        : 0xFF353535;

        graphics.fill(
                x,
                y,
                x + BUTTON_WIDTH,
                y + BUTTON_HEIGHT,
                border);

        graphics.fill(
                x + 1,
                y + 1,
                x + BUTTON_WIDTH - 1,
                y + BUTTON_HEIGHT - 1,
                background);

        graphics.drawString(
                font,
                aptitude.getName(),
                x + 5,
                y + 4,
                atMaximum
                        ? GRAY
                        : WHITE,
                false);

        String level =
                Integer.toString(current);

        graphics.drawString(
                font,
                level,
                x + BUTTON_WIDTH
                        - 5
                        - font.width(level),
                y + 4,
                atMaximum
                        ? GRAY
                        : GREEN,
                false);

        if (hover && atMaximum) {

            graphics.renderTooltip(
                    font,
                    Component.literal(
                                    "Maximum aptitude level reached")
                            .withStyle(
                                    ChatFormatting.GRAY),
                    mouseX,
                    mouseY);
        }
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

        if (isBackHovered(
                left,
                top,
                mouseX,
                mouseY)) {

            minecraft.setScreen(parent);
            return true;
        }

        List<Aptitude> aptitudes =
                List.of(
                        RegistryAptitudes.STRENGTH,
                        RegistryAptitudes.DEXTERITY,
                        RegistryAptitudes.CONSTITUTION,
                        RegistryAptitudes.INTELLIGENCE,
                        RegistryAptitudes.WISDOM,
                        RegistryAptitudes.CHARISMA);

        for (int i = 0;
             i < aptitudes.size();
             i++) {

            int column =
                    i % 2;

            int row =
                    i / 2;

            int x =
                    left + 13
                            + column * 92;

            int y =
                    top + 42
                            + row * 28;

            if (!isMouseWithin(
                    x,
                    y,
                    mouseX,
                    mouseY,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT)) {

                continue;
            }

            Aptitude aptitude =
                    aptitudes.get(i);

            if (progress.getAptitudeLevel(aptitude)
                    >= CommonConfigService
                    .aptitudeMaxLevel()) {

                return true;
            }

            PlayerProgressClientRequests
                    .requestFeatSelection(
                            feat.id(),
                            aptitude.getName());

            /*
             * Return to the original progression screen,
             * not merely the feat list.
             */
            if (parent instanceof FeatSelectionScreen) {
                minecraft.setScreen(
                        new AptitudesOverviewScreen());
            } else {
                minecraft.setScreen(parent);
            }

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

    private void renderBackButton(
            GuiGraphics graphics,
            int left,
            int top,
            int mouseX,
            int mouseY) {

        int x =
                left + WIDTH / 2 - 28;

        int y =
                top + 132;

        boolean hover =
                isBackHovered(
                        left,
                        top,
                        mouseX,
                        mouseY);

        graphics.fill(
                x,
                y,
                x + 56,
                y + 13,
                hover
                        ? 0xFF777777
                        : 0xFF555555);

        graphics.fill(
                x + 1,
                y + 1,
                x + 55,
                y + 12,
                0xFF353535);

        Component text =
                Component.literal("Back");

        graphics.drawString(
                font,
                text,
                x + 28
                        - font.width(text) / 2,
                y + 3,
                hover
                        ? WHITE
                        : GRAY,
                false);
    }

    private boolean isBackHovered(
            int left,
            int top,
            double mouseX,
            double mouseY) {

        return isMouseWithin(
                left + WIDTH / 2 - 28,
                top + 132,
                mouseX,
                mouseY,
                56,
                13);
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