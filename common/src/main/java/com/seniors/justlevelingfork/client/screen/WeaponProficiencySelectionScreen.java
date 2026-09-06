package com.seniors.justlevelingfork.client.screen;

import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.network.FeatDefinitionsSyncPayload;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WeaponProficiencySelectionScreen extends Screen {

    private static final int WIDTH = 260;
    private static final int HEIGHT = 220;

    private static final int ENTRY_WIDTH = 112;
    private static final int ENTRY_HEIGHT = 18;
    private static final int ENTRY_SPACING_Y = 20;
    private static final int COLUMN_SPACING = 118;
    private static final int VISIBLE_ROWS = 7;

    private static final int WHITE = 0xF0F0F0;
    private static final int GRAY = 0xAAAAAA;
    private static final int GREEN = 0x70E050;
    private static final int RED = 0xE06060;
    private static final int TEXT = 0x303030;

    private final Screen parent;
    private final FeatDefinitionsSyncPayload.Definition feat;
    private final String abilityChoice;

    private final Set<ResourceLocation> selected =
            new LinkedHashSet<>();

    private int scrollRow;

    public WeaponProficiencySelectionScreen(
            Screen parent,
            FeatDefinitionsSyncPayload.Definition feat,
            String abilityChoice) {

        super(Component.literal("Choose Weapons"));

        this.parent = parent;
        this.feat = feat;
        this.abilityChoice =
                abilityChoice == null
                        ? ""
                        : abilityChoice.trim();
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
                Component.literal(feat.name())
                        .withStyle(ChatFormatting.BOLD),
                left + WIDTH / 2,
                top + 9,
                TEXT);

        FeatDefinitionsSyncPayload.WeaponProficiencyChoice choice =
                feat.weaponProficiencyChoice();

        if (choice == null) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Weapon choice data unavailable"),
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

        drawCentered(
                graphics,
                Component.literal(
                        "Selected: "
                                + selected.size()
                                + "/"
                                + choice.choicesRequired()),
                left + WIDTH / 2,
                top + 23,
                selected.size() == choice.choicesRequired()
                        ? GREEN
                        : TEXT);

        List<ResourceLocation> allowed =
                choice.allowedProficiencies();

        clampScroll(allowed.size());

        int startIndex =
                scrollRow * 2;

        int endIndex =
                Math.min(
                        allowed.size(),
                        startIndex + VISIBLE_ROWS * 2);

        for (int index = startIndex;
             index < endIndex;
             index++) {

            int visibleIndex =
                    index - startIndex;

            int column =
                    visibleIndex % 2;

            int row =
                    visibleIndex / 2;

            int x =
                    left + 14
                            + column * COLUMN_SPACING;

            int y =
                    top + 40
                            + row * ENTRY_SPACING_Y;

            renderWeaponEntry(
                    graphics,
                    allowed.get(index),
                    choice,
                    x,
                    y,
                    mouseX,
                    mouseY);
        }

        if (totalRows(allowed.size()) > VISIBLE_ROWS) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Mouse wheel to scroll"),
                    left + WIDTH / 2,
                    top + 183,
                    GRAY);
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
                mouseY,
                choice);

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

    private void renderWeaponEntry(
            GuiGraphics graphics,
            ResourceLocation id,
            FeatDefinitionsSyncPayload.WeaponProficiencyChoice choice,
            int x,
            int y,
            int mouseX,
            int mouseY) {

        boolean isSelected =
                selected.contains(id);

        boolean canSelect =
                isSelected
                        || selected.size()
                        < choice.choicesRequired();

        boolean hover =
                isMouseWithin(
                        x,
                        y,
                        mouseX,
                        mouseY,
                        ENTRY_WIDTH,
                        ENTRY_HEIGHT);

        int border =
                isSelected
                        ? 0xFF5F7750
                        : canSelect
                        ? (hover
                        ? 0xFF88AA66
                        : 0xFF666666)
                        : 0xFF555555;

        int background =
                hover
                        ? 0xFF484848
                        : 0xFF353535;

        graphics.fill(
                x,
                y,
                x + ENTRY_WIDTH,
                y + ENTRY_HEIGHT,
                border);

        graphics.fill(
                x + 1,
                y + 1,
                x + ENTRY_WIDTH - 1,
                y + ENTRY_HEIGHT - 1,
                background);

        String name =
                displayName(id);

        String text =
                isSelected
                        ? "[X] " + name
                        : name;

        graphics.drawString(
                font,
                text,
                x + 5,
                y + 5,
                isSelected
                        ? GREEN
                        : canSelect
                        ? WHITE
                        : GRAY,
                false);

        if (hover) {

            graphics.renderTooltip(
                    font,
                    Component.literal(
                                    id.toString())
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

        FeatDefinitionsSyncPayload.WeaponProficiencyChoice choice =
                feat.weaponProficiencyChoice();

        if (choice == null) {
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

        if (isResetHovered(
                left,
                top,
                mouseX,
                mouseY)) {

            selected.clear();
            return true;
        }

        if (isConfirmHovered(
                left,
                top,
                mouseX,
                mouseY)) {

            if (selected.size()
                    != choice.choicesRequired()) {

                return true;
            }

            String weapons =
                    selected.stream()
                            .map(ResourceLocation::toString)
                            .collect(
                                    Collectors.joining(","));

            if (weapons.isBlank()) {
                return true;
            }

            String submittedChoice =
                    choice.choiceKey()
                            + "="
                            + weapons;

            if (!abilityChoice.isBlank()) {

                submittedChoice =
                        "ability="
                                + abilityChoice
                                + ";"
                                + submittedChoice;
            }

            PlayerProgressClientRequests
                    .requestFeatSelection(
                            feat.id(),
                            submittedChoice);

            minecraft.setScreen(
                    new AptitudesOverviewScreen());

            return true;
        }

        List<ResourceLocation> allowed =
                choice.allowedProficiencies();

        clampScroll(allowed.size());

        int startIndex =
                scrollRow * 2;

        int endIndex =
                Math.min(
                        allowed.size(),
                        startIndex + VISIBLE_ROWS * 2);

        for (int index = startIndex;
             index < endIndex;
             index++) {

            int visibleIndex =
                    index - startIndex;

            int column =
                    visibleIndex % 2;

            int row =
                    visibleIndex / 2;

            int x =
                    left + 14
                            + column * COLUMN_SPACING;

            int y =
                    top + 40
                            + row * ENTRY_SPACING_Y;

            if (!isMouseWithin(
                    x,
                    y,
                    mouseX,
                    mouseY,
                    ENTRY_WIDTH,
                    ENTRY_HEIGHT)) {

                continue;
            }

            ResourceLocation id =
                    allowed.get(index);

            if (selected.contains(id)) {

                selected.remove(id);

            } else if (selected.size()
                    < choice.choicesRequired()) {

                selected.add(id);
            }

            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta) {

        FeatDefinitionsSyncPayload.WeaponProficiencyChoice choice =
                feat.weaponProficiencyChoice();

        if (choice == null) {
            return false;
        }

        int maximum =
                Math.max(
                        0,
                        totalRows(
                                choice.allowedProficiencies().size())
                                - VISIBLE_ROWS);

        if (delta < 0) {

            scrollRow =
                    Math.min(
                            maximum,
                            scrollRow + 1);

        } else if (delta > 0) {

            scrollRow =
                    Math.max(
                            0,
                            scrollRow - 1);
        }

        return true;
    }

    private void clampScroll(
            int optionCount) {

        int maximum =
                Math.max(
                        0,
                        totalRows(optionCount)
                                - VISIBLE_ROWS);

        scrollRow =
                Math.max(
                        0,
                        Math.min(
                                scrollRow,
                                maximum));
    }

    private static int totalRows(
            int optionCount) {

        return (Math.max(0, optionCount) + 1) / 2;
    }

    private static String displayName(
            ResourceLocation id) {

        if (id == null) {
            return "Unknown";
        }

        String path =
                id.getPath();

        int separator =
                path.lastIndexOf('/');

        if (separator >= 0
                && separator < path.length() - 1) {

            path =
                    path.substring(
                            separator + 1);
        }

        String[] words =
                path.replace('_', ' ')
                        .split(" ");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)));

            if (word.length() > 1) {

                result.append(
                        word.substring(1)
                                .toLowerCase(
                                        Locale.ROOT));
            }
        }

        return result.isEmpty()
                ? id.toString()
                : result.toString();
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
                top + 197,
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
            int mouseY,
            FeatDefinitionsSyncPayload.WeaponProficiencyChoice choice) {

        boolean enabled =
                selected.size()
                        == choice.choicesRequired();

        renderSmallButton(
                graphics,
                left + WIDTH / 2 - 30,
                top + 197,
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
                top + 197,
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
                top + 197,
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
                top + 197,
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
                top + 197,
                mouseX,
                mouseY,
                54,
                14);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
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
