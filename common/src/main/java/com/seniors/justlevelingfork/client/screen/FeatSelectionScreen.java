package com.seniors.justlevelingfork.client.screen;

import com.seniors.justlevelingfork.common.feat.FeatClientState;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.network.FeatDefinitionsSyncPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FeatSelectionScreen extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 190;

    private static final int ENTRY_X_OFFSET = 12;
    private static final int ENTRY_Y_OFFSET = 42;

    private static final int ENTRY_WIDTH = 196;
    private static final int ENTRY_HEIGHT = 18;
    private static final int ENTRY_SPACING = 20;

    private static final int VISIBLE_ENTRIES = 6;

    private static final int WHITE = 0xF0F0F0;
    private static final int GRAY = 0xAAAAAA;
    private static final int GREEN = 0x70E050;
    private static final int RED = 0xE06060;
    private static final int TEXT = 0x303030;

    private final Screen parent;

    private int scrollOffset;

    public FeatSelectionScreen(Screen parent) {
        super(Component.literal("Choose Feat"));
        this.parent = parent;
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

        renderPanel(
                graphics,
                left,
                top);

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        drawCentered(
                graphics,
                Component.literal("Choose Feat")
                        .withStyle(ChatFormatting.BOLD),
                left + WIDTH / 2,
                top + 9,
                TEXT);

        if (progress == null) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Progression data unavailable"),
                    left + WIDTH / 2,
                    top + 30,
                    TEXT);

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
                        "Pending Advancements: "
                                + progress.getPendingAdvancements()),
                left + WIDTH / 2,
                top + 22,
                TEXT);

        List<FeatDefinitionsSyncPayload.Definition> feats =
                sortedFeats();

        clampScroll(feats.size());

        int end =
                Math.min(
                        feats.size(),
                        scrollOffset + VISIBLE_ENTRIES);

        for (int visibleIndex = scrollOffset;
             visibleIndex < end;
             visibleIndex++) {

            int row =
                    visibleIndex - scrollOffset;

            int x =
                    left + ENTRY_X_OFFSET;

            int y =
                    top
                            + ENTRY_Y_OFFSET
                            + row * ENTRY_SPACING;

            renderFeatEntry(
                    graphics,
                    progress,
                    feats.get(visibleIndex),
                    x,
                    y,
                    mouseX,
                    mouseY);
        }

        if (feats.size() > VISIBLE_ENTRIES) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Mouse wheel to scroll"),
                    left + WIDTH / 2,
                    top + 166,
                    GRAY);
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

    private void renderPanel(
            GuiGraphics graphics,
            int left,
            int top) {

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
    }

    private void renderFeatEntry(
            GuiGraphics graphics,
            PlayerProgress progress,
            FeatDefinitionsSyncPayload.Definition feat,
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
                        ENTRY_WIDTH,
                        ENTRY_HEIGHT);

        boolean available =
                isClientAvailable(
                        progress,
                        feat);

        int border =
                available
                        ? (hover
                        ? 0xFF88AA66
                        : 0xFF5F7750)
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

        int rank =
                progress.getFeatRank(
                        feat.id().toString());

        graphics.drawString(
                font,
                feat.name(),
                x + 5,
                y + 5,
                available
                        ? WHITE
                        : GRAY,
                false);

        String rankText;

        if (feat.repeatable()) {

            if (feat.maxRank() <= 0) {
                rankText =
                        "Rank " + rank;
            } else {
                rankText =
                        rank + "/" + feat.maxRank();
            }

        } else {

            rankText =
                    rank > 0
                            ? "Taken"
                            : "";
        }

        if (!rankText.isEmpty()) {

            graphics.drawString(
                    font,
                    rankText,
                    x + ENTRY_WIDTH
                            - 5
                            - font.width(rankText),
                    y + 5,
                    available
                            ? GREEN
                            : GRAY,
                    false);
        }

        if (!hover) {
            return;
        }

        List<Component> tooltip =
                new ArrayList<>();

        tooltip.add(
                Component.literal(
                                feat.name())
                        .withStyle(
                                available
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.GRAY));

        if (feat.description() != null
                && !feat.description().isBlank()) {

            tooltip.add(
                    Component.literal(
                                    feat.description())
                            .withStyle(
                                    ChatFormatting.GRAY));
        }

        if (progress.getCharacterLevel()
                < feat.minimumCharacterLevel()) {

            tooltip.add(
                    Component.literal(
                                    "Requires Character Level "
                                            + feat.minimumCharacterLevel())
                            .withStyle(
                                    ChatFormatting.RED));
        }

        if (!canGainRank(
                progress,
                feat)) {

            tooltip.add(
                    Component.literal(
                                    "Maximum rank reached")
                            .withStyle(
                                    ChatFormatting.RED));
        }

        /*
         * Client availability is informational only.
         * Server is still authoritative.
         */
        graphics.renderComponentTooltip(
                font,
                tooltip,
                mouseX,
                mouseY);
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

        if (progress.getPendingAdvancements() <= 0) {
            minecraft.setScreen(parent);
            return true;
        }

        List<FeatDefinitionsSyncPayload.Definition> feats =
                sortedFeats();

        clampScroll(feats.size());

        int end =
                Math.min(
                        feats.size(),
                        scrollOffset + VISIBLE_ENTRIES);

        for (int visibleIndex = scrollOffset;
             visibleIndex < end;
             visibleIndex++) {

            int row =
                    visibleIndex - scrollOffset;

            int x =
                    left + ENTRY_X_OFFSET;

            int y =
                    top
                            + ENTRY_Y_OFFSET
                            + row * ENTRY_SPACING;

            if (!isMouseWithin(
                    x,
                    y,
                    mouseX,
                    mouseY,
                    ENTRY_WIDTH,
                    ENTRY_HEIGHT)) {

                continue;
            }

            FeatDefinitionsSyncPayload.Definition feat =
                    feats.get(visibleIndex);

            if (!isClientAvailable(
                    progress,
                    feat)) {

                return true;
            }

            /*
             * ASI requires a second choice screen.
             *
             * Other effect types can currently be submitted
             * immediately with an empty choice.
             */
            if (isAbilityScoreImprovement(
                    feat.effectType())) {

                minecraft.setScreen(
                        new AbilityScoreSelectionScreen(
                                this,
                                feat));

                return true;
            }

            PlayerProgressClientRequests
                    .requestFeatSelection(
                            feat.id(),
                            "");

            minecraft.setScreen(parent);

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

        List<FeatDefinitionsSyncPayload.Definition> feats =
                sortedFeats();

        int maximum =
                Math.max(
                        0,
                        feats.size() - VISIBLE_ENTRIES);

        if (delta < 0) {
            scrollOffset =
                    Math.min(
                            maximum,
                            scrollOffset + 1);

        } else if (delta > 0) {

            scrollOffset =
                    Math.max(
                            0,
                            scrollOffset - 1);
        }

        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isClientAvailable(
            PlayerProgress progress,
            FeatDefinitionsSyncPayload.Definition feat) {

        return progress.getPendingAdvancements() > 0
                && progress.getCharacterLevel()
                >= feat.minimumCharacterLevel()
                && canGainRank(
                progress,
                feat);
    }

    private boolean canGainRank(
            PlayerProgress progress,
            FeatDefinitionsSyncPayload.Definition feat) {

        int rank =
                progress.getFeatRank(
                        feat.id().toString());

        if (rank <= 0) {
            return true;
        }

        if (!feat.repeatable()) {
            return false;
        }

        return feat.maxRank() <= 0
                || rank < feat.maxRank();
    }

    private static boolean isAbilityScoreImprovement(
            ResourceLocation effectType) {

        return effectType != null
                && effectType.toString()
                .equals(
                        "justlevelingfork:"
                                + "ability_score_improvement");
    }

    private List<FeatDefinitionsSyncPayload.Definition> sortedFeats() {

        return FeatClientState.values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                FeatDefinitionsSyncPayload.Definition::name,
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void clampScroll(
            int featCount) {

        int maximum =
                Math.max(
                        0,
                        featCount - VISIBLE_ENTRIES);

        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                scrollOffset,
                                maximum));
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
                top + 173;

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
                Component.literal("< Back");

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
                top + 173,
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