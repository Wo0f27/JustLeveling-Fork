package com.seniors.justlevelingfork.client.screen;

import com.seniors.justlevelingfork.common.player.CharacterAdminClientRequests;
import com.seniors.justlevelingfork.common.player.CharacterAdminClientState;
import com.seniors.justlevelingfork.common.player.CharacterExperienceService;
import com.seniors.justlevelingfork.common.player.ClassProgressionService;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CharacterAdminScreen extends Screen {

    private static final int WIDTH = 240;
    private static final int HEIGHT = 240;

    private static final int FONT_COLOR = 0x3E3E3E;
    private static final int WHITE = 0xF0F0F0;
    private static final int GRAY = 0xAAAAAA;
    private static final int GREEN = 0x55DD55;

    private final Screen parent;

    private int selectedClassIndex;
    private boolean selectionInitialized;

    public CharacterAdminScreen(
            Screen parent) {

        super(Component.literal(
                "JLF Character Admin"));

        this.parent = parent;
    }

    @Override
    protected void init() {

        if (!selectionInitialized) {

            initializeClassSelection();
            selectionInitialized = true;
        }

        super.init();
    }

    private void initializeClassSelection() {

        List<ResourceLocation> classes =
                RegistryClasses.values();

        if (classes.isEmpty()) {
            selectedClassIndex = 0;
            return;
        }

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        if (progress == null) {
            selectedClassIndex = 0;
            return;
        }

        ResourceLocation starting =
                ResourceLocation.tryParse(
                        progress.getStartingClass());

        int index =
                starting == null
                        ? -1
                        : classes.indexOf(starting);

        selectedClassIndex =
                index >= 0
                        ? index
                        : 0;
    }

    @Override
    public void tick() {

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
                top + 9,
                FONT_COLOR);

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        if (progress != null) {

            drawCentered(
                    graphics,
                    Component.literal(
                            "Level "
                                    + progress
                                    .getCharacterLevel()
                                    + "   XP "
                                    + progress
                                    .getCharacterXp()),
                    left + WIDTH / 2,
                    top + 25,
                    FONT_COLOR);

            drawCentered(
                    graphics,
                    Component.literal(
                            "Starting: "
                                    + displayName(
                                    ResourceLocation.tryParse(
                                            progress
                                                    .getStartingClass()))
                                    + "   Abilities: "
                                    + (progress
                                    .isStartingAbilitiesAssigned()
                                    ? "Assigned"
                                    : "Not Assigned")),
                    left + WIDTH / 2,
                    top + 37,
                    FONT_COLOR);
        }

        renderClassSelector(
                graphics,
                left,
                top,
                mouseX,
                mouseY);

        ResourceLocation selected =
                selectedClass();

        renderButton(
                graphics,
                left + 16,
                top + 82,
                208,
                16,
                "Start as "
                        + displayName(selected),
                true,
                mouseX,
                mouseY);

        boolean hasStartingClass =
                progress != null
                        && ResourceLocation.tryParse(
                        progress.getStartingClass())
                        != null;

        renderButton(
                graphics,
                left + 16,
                top + 103,
                208,
                16,
                "Apply Recommended Abilities",
                hasStartingClass,
                mouseX,
                mouseY);

        graphics.drawString(
                font,
                Component.literal("Character XP")
                        .withStyle(
                                ChatFormatting.BOLD),
                left + 16,
                top + 127,
                FONT_COLOR,
                false);

        boolean canAddXp =
                progress != null
                        && progress.getCharacterLevel() > 0
                        && progress.getCharacterLevel()
                        < ClassProgressionService
                        .MAX_CHARACTER_LEVEL;

        renderButton(
                graphics,
                left + 16,
                top + 140,
                62,
                16,
                "+300",
                canAddXp,
                mouseX,
                mouseY);

        renderButton(
                graphics,
                left + 89,
                top + 140,
                62,
                16,
                "+1,000",
                canAddXp,
                mouseX,
                mouseY);

        renderButton(
                graphics,
                left + 162,
                top + 140,
                62,
                16,
                "+10,000",
                canAddXp,
                mouseX,
                mouseY);

        long xpNeeded =
                progress == null
                        ? 0L
                        : CharacterExperienceService
                        .xpNeededForNextLevel(
                                progress);

        String nextText =
                xpNeeded > 0L
                        ? "XP to Next Level (+"
                        + String.format(
                        Locale.ROOT,
                        "%,d",
                        xpNeeded)
                        + ")"
                        : "XP to Next Level";

        renderButton(
                graphics,
                left + 16,
                top + 161,
                208,
                16,
                nextText,
                canAddXp
                        && xpNeeded > 0L,
                mouseX,
                mouseY);

        renderButton(
                graphics,
                left + 16,
                top + 184,
                208,
                16,
                "Penalty Debug",
                progress != null,
                mouseX,
                mouseY);


        renderButton(
                graphics,
                left + 16,
                top + 207,
                100,
                16,
                "Reset Character",
                progress != null,
                mouseX,
                mouseY);

        renderButton(
                graphics,
                left + 124,
                top + 187,
                100,
                16,
                "< Back",
                true,
                mouseX,
                mouseY);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick);
    }

    private void renderClassSelector(
            GuiGraphics graphics,
            int left,
            int top,
            int mouseX,
            int mouseY) {

        int y = top + 57;

        renderButton(
                graphics,
                left + 16,
                y,
                18,
                18,
                "<",
                true,
                mouseX,
                mouseY);

        graphics.fill(
                left + 39,
                y,
                left + 201,
                y + 18,
                0xFF555555);

        graphics.fill(
                left + 40,
                y + 1,
                left + 200,
                y + 17,
                0xFF353535);

        drawCentered(
                graphics,
                Component.literal(
                                displayName(
                                        selectedClass()))
                        .withStyle(
                                ChatFormatting.GOLD),
                left + 120,
                y + 5,
                WHITE);

        renderButton(
                graphics,
                left + 206,
                y,
                18,
                18,
                ">",
                true,
                mouseX,
                mouseY);
    }

    private void renderButton(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean enabled,
            int mouseX,
            int mouseY) {

        boolean hover =
                enabled
                        && isMouseWithin(
                        x,
                        y,
                        mouseX,
                        mouseY,
                        width,
                        height);

        int borderColor;

        if (!enabled) {
            borderColor = 0xFF555555;
        } else if (hover) {
            borderColor = 0xFF888888;
        } else {
            borderColor = 0xFF666666;
        }

        int backgroundColor;

        if (!enabled) {
            backgroundColor = 0xFF2B2B2B;
        } else if (hover) {
            backgroundColor = 0xFF484848;
        } else {
            backgroundColor = 0xFF353535;
        }

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                borderColor);

        graphics.fill(
                x + 1,
                y + 1,
                x + width - 1,
                y + height - 1,
                backgroundColor);

        Component text =
                Component.literal(label);

        graphics.drawString(
                font,
                text,
                x + width / 2
                        - font.width(text) / 2,
                y + (height - 8) / 2,
                enabled
                        ? hover
                        ? WHITE
                        : 0xFFDDDDDD
                        : 0xFF777777,
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

        PlayerProgress progress =
                PlayerProgressClientState.get()
                        .orElse(null);

        if (isMouseWithin(
                left + 16,
                top + 57,
                mouseX,
                mouseY,
                18,
                18)) {

            cycleClass(-1);
            return true;
        }

        if (isMouseWithin(
                left + 206,
                top + 57,
                mouseX,
                mouseY,
                18,
                18)) {

            cycleClass(1);
            return true;
        }

        if (progress != null
                && isMouseWithin(
                left + 16,
                top + 184,
                mouseX,
                mouseY,
                208,
                16)) {

            CharacterAdminClientRequests
                    .requestPenaltyDebug();

            return true;
        }

        if (isMouseWithin(
                left + 16,
                top + 82,
                mouseX,
                mouseY,
                208,
                16)) {

            CharacterAdminClientRequests
                    .requestSetStartingClass(
                            selectedClass());

            return true;
        }

        if (progress != null
                && ResourceLocation.tryParse(
                progress.getStartingClass())
                != null
                && isMouseWithin(
                left + 16,
                top + 103,
                mouseX,
                mouseY,
                208,
                16)) {

            CharacterAdminClientRequests
                    .requestRecommendedAbilities();

            return true;
        }

        boolean canAddXp =
                progress != null
                        && progress.getCharacterLevel() > 0
                        && progress.getCharacterLevel()
                        < ClassProgressionService
                        .MAX_CHARACTER_LEVEL;

        if (canAddXp
                && isMouseWithin(
                left + 16,
                top + 140,
                mouseX,
                mouseY,
                62,
                16)) {

            CharacterAdminClientRequests
                    .requestAddXp(300L);

            return true;
        }

        if (canAddXp
                && isMouseWithin(
                left + 89,
                top + 140,
                mouseX,
                mouseY,
                62,
                16)) {

            CharacterAdminClientRequests
                    .requestAddXp(1_000L);

            return true;
        }

        if (canAddXp
                && isMouseWithin(
                left + 162,
                top + 140,
                mouseX,
                mouseY,
                62,
                16)) {

            CharacterAdminClientRequests
                    .requestAddXp(10_000L);

            return true;
        }

        long xpNeeded =
                progress == null
                        ? 0L
                        : CharacterExperienceService
                        .xpNeededForNextLevel(
                                progress);

        if (canAddXp
                && xpNeeded > 0L
                && isMouseWithin(
                left + 16,
                top + 161,
                mouseX,
                mouseY,
                208,
                16)) {

            CharacterAdminClientRequests
                    .requestXpToNextLevel();

            return true;
        }

        if (progress != null
                && isMouseWithin(
                left + 16,
                top + 184,
                mouseX,
                mouseY,
                208,
                16)) {

            CharacterAdminClientRequests
                    .requestPenaltyDebug();

            return true;
        }

        if (progress != null
                && isMouseWithin(
                left + 16,
                top + 207,
                mouseX,
                mouseY,
                100,
                16)) {

            CharacterAdminClientRequests
                    .requestResetCharacter();

            return true;
        }

        if (isMouseWithin(
                left + 124,
                top + 187,
                mouseX,
                mouseY,
                100,
                16)) {

            minecraft.setScreen(parent);
            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button);
    }

    private void cycleClass(
            int direction) {

        List<ResourceLocation> classes =
                RegistryClasses.values();

        if (classes.isEmpty()) {
            return;
        }

        selectedClassIndex =
                Math.floorMod(
                        selectedClassIndex
                                + direction,
                        classes.size());
    }

    private ResourceLocation selectedClass() {

        List<ResourceLocation> classes =
                RegistryClasses.values();

        if (classes.isEmpty()) {
            return null;
        }

        selectedClassIndex =
                Math.floorMod(
                        selectedClassIndex,
                        classes.size());

        return classes.get(
                selectedClassIndex);
    }

    private static String displayName(
            ResourceLocation id) {

        if (id == null) {
            return "None";
        }

        String path =
                id.getPath();

        if (path.isBlank()) {
            return "None";
        }

        String normalized =
                path.replace(
                        '_',
                        ' ');

        return normalized
                .substring(0, 1)
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

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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