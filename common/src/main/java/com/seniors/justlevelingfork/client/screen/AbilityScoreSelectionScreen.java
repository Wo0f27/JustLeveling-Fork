package com.seniors.justlevelingfork.client.screen;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.AbilityScoreClientState;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.network.FeatDefinitionsSyncPayload;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AbilityScoreSelectionScreen extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 180;

    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 22;

    private static final int WHITE = 0xF0F0F0;
    private static final int GRAY = 0xAAAAAA;
    private static final int GREEN = 0x70E050;
    private static final int RED = 0xE06060;
    private static final int TEXT = 0x303030;

    private final Screen parent;
    private final FeatDefinitionsSyncPayload.Definition feat;

    private final Map<String, Integer> allocations =
            new LinkedHashMap<>();

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

        FeatDefinitionsSyncPayload.AbilityScoreChoice choice =
                feat.abilityScoreChoice();

        if (choice == null) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Ability choice data unavailable"),
                    left + WIDTH / 2,
                    top + 28,
                    RED);

            super.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick);

            return;
        }

        int remaining =
                choice.points()
                        - allocatedPoints();

        drawCentered(
                graphics,
                Component.literal(
                        "Points remaining: "
                                + remaining),
                left + WIDTH / 2,
                top + 23,
                remaining == 0
                        ? GREEN
                        : TEXT);

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
                    RED);

            super.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick);

            return;
        }

        List<Aptitude> aptitudes =
                aptitudes();

        for (int i = 0;
             i < aptitudes.size();
             i++) {

            int column =
                    i % 2;

            int row =
                    i / 2;

            int x =
                    left + 14
                            + column * 102;

            int y =
                    top + 42
                            + row * 29;

            renderAbilityButton(
                    graphics,
                    progress,
                    choice,
                    aptitudes.get(i),
                    x,
                    y,
                    mouseX,
                    mouseY);
        }

        renderResetButton(
                graphics,
                left,
                top,
                mouseX,
                mouseY);

        renderConfirmButton(
                graphics,
                left,
                top,
                mouseX,
                mouseY);

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
            FeatDefinitionsSyncPayload.AbilityScoreChoice choice,
            Aptitude aptitude,
            int x,
            int y,
            int mouseX,
            int mouseY) {

        String name =
                aptitude.getName()
                        .toLowerCase(
                                Locale.ROOT);

        int allocated =
                allocations.getOrDefault(
                        name,
                        0);

        int currentEffective =
                effectiveScore(
                        progress,
                        aptitude);

        int projected =
                currentEffective
                        + allocated;

        boolean allowed =
                isAllowed(
                        choice,
                        name);

        boolean canIncrease =
                allowed
                        && allocatedPoints()
                        < choice.points()
                        && projected
                        < choice.maxScore()
                        && progress.getAptitudeLevel(
                        aptitude)
                        + allocated
                        < CommonConfigService
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
                !allowed
                        ? 0xFF555555
                        : canIncrease
                        ? (hover
                        ? 0xFF88AA66
                        : 0xFF5F7750)
                        : 0xFF666666;

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

        String displayName =
                shortName(
                        aptitude);

        graphics.drawString(
                font,
                displayName,
                x + 5,
                y + 4,
                allowed
                        ? WHITE
                        : GRAY,
                false);

        String scoreText =
                Integer.toString(
                        projected);

        if (allocated > 0) {

            scoreText +=
                    " (+" + allocated + ")";
        }

        graphics.drawString(
                font,
                scoreText,
                x + BUTTON_WIDTH
                        - 5
                        - font.width(scoreText),
                y + 4,
                allocated > 0
                        ? GREEN
                        : allowed
                        ? WHITE
                        : GRAY,
                false);

        if (hover && !allowed) {

            graphics.renderTooltip(
                    font,
                    Component.literal(
                                    "This feat cannot increase "
                                            + aptitude.getName())
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

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        FeatDefinitionsSyncPayload.AbilityScoreChoice choice =
                feat.abilityScoreChoice();

        if (progress == null
                || choice == null) {

            return false;
        }

        int left = left();
        int top = top();

        if (button == 0
                && isBackHovered(
                left,
                top,
                mouseX,
                mouseY)) {

            minecraft.setScreen(
                    parent);

            return true;
        }

        if (button == 0
                && isResetHovered(
                left,
                top,
                mouseX,
                mouseY)) {

            allocations.clear();

            return true;
        }

        if (button == 0
                && isConfirmHovered(
                left,
                top,
                mouseX,
                mouseY)) {

            if (allocatedPoints()
                    != choice.points()) {

                return true;
            }

            String allocation =
                    buildChoiceString();

            if (allocation.isBlank()) {
                return true;
            }

            PlayerProgressClientRequests
                    .requestFeatSelection(
                            feat.id(),
                            allocation);

            minecraft.setScreen(
                    new AptitudesOverviewScreen());

            return true;
        }

        List<Aptitude> aptitudes =
                aptitudes();

        for (int i = 0;
             i < aptitudes.size();
             i++) {

            int column =
                    i % 2;

            int row =
                    i / 2;

            int x =
                    left + 14
                            + column * 102;

            int y =
                    top + 42
                            + row * 29;

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

            String name =
                    aptitude.getName()
                            .toLowerCase(
                                    Locale.ROOT);

            /*
             * Right click removes one allocated point.
             */
            if (button == 1) {

                int current =
                        allocations.getOrDefault(
                                name,
                                0);

                if (current <= 1) {

                    allocations.remove(
                            name);

                } else {

                    allocations.put(
                            name,
                            current - 1);
                }

                return true;
            }

            if (button != 0) {
                return true;
            }

            if (!isAllowed(
                    choice,
                    name)) {

                return true;
            }

            if (allocatedPoints()
                    >= choice.points()) {

                return true;
            }

            int allocated =
                    allocations.getOrDefault(
                            name,
                            0);

            int effective =
                    effectiveScore(
                            progress,
                            aptitude);

            if (effective + allocated
                    >= choice.maxScore()) {

                return true;
            }

            if (progress.getAptitudeLevel(
                    aptitude)
                    + allocated
                    >= CommonConfigService
                    .aptitudeMaxLevel()) {

                return true;
            }

            allocations.put(
                    name,
                    allocated + 1);

            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button);
    }

    private boolean isAllowed(
            FeatDefinitionsSyncPayload.AbilityScoreChoice choice,
            String ability) {

        if (choice.allowedAbilities()
                .isEmpty()) {

            return true;
        }

        return choice.allowedAbilities()
                .stream()
                .anyMatch(name ->
                        name.equalsIgnoreCase(
                                ability));
    }

    private int effectiveScore(
            PlayerProgress progress,
            Aptitude aptitude) {

        Integer synced =
                AbilityScoreClientState.get(
                        aptitude);

        if (synced != null) {
            return synced;
        }

        /*
         * Fallback if the effective-score packet
         * has not arrived yet.
         */
        return progress.getAptitudeLevel(
                aptitude) + 9;
    }

    private int allocatedPoints() {

        return allocations.values()
                .stream()
                .mapToInt(
                        Integer::intValue)
                .sum();
    }

    private String buildChoiceString() {

        return allocations.entrySet()
                .stream()
                .filter(entry ->
                        entry.getValue() > 0)
                .map(entry ->
                        entry.getKey()
                                + ":"
                                + entry.getValue())
                .reduce(
                        (left, right) ->
                                left + "," + right)
                .orElse("");
    }

    private List<Aptitude> aptitudes() {

        return List.of(
                RegistryAptitudes.STRENGTH,
                RegistryAptitudes.DEXTERITY,
                RegistryAptitudes.CONSTITUTION,
                RegistryAptitudes.INTELLIGENCE,
                RegistryAptitudes.WISDOM,
                RegistryAptitudes.CHARISMA);
    }

    private static String shortName(
            Aptitude aptitude) {

        String name =
                aptitude.getName();

        if (name == null
                || name.length() < 3) {

            return name;
        }

        return name.substring(
                        0,
                        3)
                .toUpperCase(
                        Locale.ROOT);
    }

    private void renderResetButton(
            GuiGraphics graphics,
            int left,
            int top,
            int mouseX,
            int mouseY) {

        renderSmallButton(
                graphics,
                left + 14,
                top + 136,
                54,
                "Reset",
                isResetHovered(
                        left,
                        top,
                        mouseX,
                        mouseY),
                true);
    }

    private void renderConfirmButton(
            GuiGraphics graphics,
            int left,
            int top,
            int mouseX,
            int mouseY) {

        FeatDefinitionsSyncPayload.AbilityScoreChoice choice =
                feat.abilityScoreChoice();

        boolean enabled =
                choice != null
                        && allocatedPoints()
                        == choice.points();

        renderSmallButton(
                graphics,
                left + WIDTH / 2 - 30,
                top + 136,
                60,
                "Confirm",
                isConfirmHovered(
                        left,
                        top,
                        mouseX,
                        mouseY),
                enabled);
    }

    private void renderBackButton(
            GuiGraphics graphics,
            int left,
            int top,
            int mouseX,
            int mouseY) {

        renderSmallButton(
                graphics,
                left + WIDTH - 68,
                top + 136,
                54,
                "< Back",
                isBackHovered(
                        left,
                        top,
                        mouseX,
                        mouseY),
                true);
    }

    private void renderSmallButton(
            GuiGraphics graphics,
            int x,
            int y,
            int buttonWidth,
            String text,
            boolean hover,
            boolean enabled) {

        graphics.fill(
                x,
                y,
                x + buttonWidth,
                y + 14,
                enabled
                        ? hover
                        ? 0xFF777777
                        : 0xFF555555
                        : 0xFF444444);

        graphics.fill(
                x + 1,
                y + 1,
                x + buttonWidth - 1,
                y + 13,
                0xFF353535);

        graphics.drawString(
                font,
                text,
                x + buttonWidth / 2
                        - font.width(text) / 2,
                y + 3,
                enabled
                        ? WHITE
                        : GRAY,
                false);
    }

    private boolean isResetHovered(
            int left,
            int top,
            double mouseX,
            double mouseY) {

        return isMouseWithin(
                left + 14,
                top + 136,
                mouseX,
                mouseY,
                54,
                14);
    }

    private boolean isConfirmHovered(
            int left,
            int top,
            double mouseX,
            double mouseY) {

        return isMouseWithin(
                left + WIDTH / 2 - 30,
                top + 136,
                mouseX,
                mouseY,
                60,
                14);
    }

    private boolean isBackHovered(
            int left,
            int top,
            double mouseX,
            double mouseY) {

        return isMouseWithin(
                left + WIDTH - 68,
                top + 136,
                mouseX,
                mouseY,
                54,
                14);
    }

    @Override
    public void onClose() {

        minecraft.setScreen(
                parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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