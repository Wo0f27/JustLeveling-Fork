package com.seniors.justlevelingfork.client.gui;

import com.seniors.justlevelingfork.client.core.Aptitudes;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import com.seniors.justlevelingfork.registry.RegistrySounds;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class ClientOverlayState {
    private static final int TITLE_TICKS = 110;
    private static final int APTITUDE_TICKS = 90;
    private static final Queue<String> TITLE_QUEUE = new ArrayDeque<>();

    private static int titleTicks;
    private static int titleScaleTicks;
    private static int aptitudeTicks;
    private static List<Aptitudes> aptitudeRequirements = List.of();

    private ClientOverlayState() {
    }

    public static void showTitleUnlock(String titleName) {
        if (titleName == null || titleName.isBlank()) {
            return;
        }

        TITLE_QUEUE.add(titleName);
        if (TITLE_QUEUE.size() == 1) {
            titleTicks = TITLE_TICKS;
            titleScaleTicks = 0;
            playTitleSound();
        }
    }

    public static void showAptitudeWarning(String restrictionId) {
        List<Aptitudes> requirements = HandlerAptitude.getValue(restrictionId);
        aptitudeRequirements = requirements == null ? List.of() : requirements;
        if (shouldHideAptitudeWarning(Minecraft.getInstance())) {
            aptitudeTicks = 0;
            aptitudeRequirements = List.of();
            return;
        }

        aptitudeTicks = APTITUDE_TICKS;
    }

    public static void tick() {
        if (titleTicks > 0) {
            titleTicks--;
        } else if (!TITLE_QUEUE.isEmpty()) {
            TITLE_QUEUE.remove();
            titleScaleTicks = 0;
            if (!TITLE_QUEUE.isEmpty()) {
                titleTicks = TITLE_TICKS;
                playTitleSound();
            }
        }

        if (aptitudeTicks > 0) {
            aptitudeTicks--;
        }
    }

    public static void render(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        renderTitle(graphics, client);
        renderAptitudeWarning(graphics, client);
    }

    private static void renderTitle(GuiGraphics graphics, Minecraft client) {
        String titleName = TITLE_QUEUE.peek();
        Title title = RegistryTitles.getTitle(titleName);
        if (title == null || titleTicks <= 0) {
            return;
        }

        titleScaleTicks += titleTicks < 20 ? -1 : 1;
        titleScaleTicks = Mth.clamp(titleScaleTicks, 0, 20);
        float titleScale = Math.max(0.1F * titleScaleTicks, 0.001F);
        float labelScale = Math.max(0.05625F * titleScaleTicks, 0.001F);

        graphics.pose().pushPose();
        graphics.pose().scale(labelScale, labelScale, 1.0F);
        int labelX = (int) (client.getWindow().getGuiScaledWidth() / labelScale / 2.0F);
        int labelY = (int) (client.getWindow().getGuiScaledHeight() / labelScale / 4.0F);
        graphics.drawCenteredString(
                client.font, Component.translatable("overlay.title.you_gain_a_title"), labelX, labelY - 10, 0xFFFFFF);
        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().scale(titleScale, titleScale, 1.0F);
        int titleX = (int) (client.getWindow().getGuiScaledWidth() / titleScale / 2.0F);
        int titleY = (int) (client.getWindow().getGuiScaledHeight() / titleScale / 4.0F);
        graphics.drawCenteredString(
                client.font,
                Component.translatable("overlay.title.format", Component.translatable(title.getKey()).withStyle(ChatFormatting.BOLD)),
                titleX,
                titleY,
                0xFFFFFF);
        graphics.pose().popPose();
    }

    private static void renderAptitudeWarning(GuiGraphics graphics, Minecraft client) {
        if (aptitudeTicks <= 0) {
            return;
        }

        if (shouldHideAptitudeWarning(client)) {
            aptitudeTicks = 0;
            aptitudeRequirements = List.of();
            return;
        }

        Component message = Component.translatable("overlay.aptitude.message");
        int centerX = client.getWindow().getGuiScaledWidth() / 2;
        int y = client.getWindow().getGuiScaledHeight() / 4;
        int halfWidth = client.font.width(message) / 2;
        int alpha = aptitudeTicks < 40 ? Mth.clamp(aptitudeTicks * 6, 0, 255) : 255;
        int panelLeft = centerX - halfWidth - 46;
        int panelRight = centerX + halfWidth + 46;
        int panelTop = y - 43;
        int panelBottom = y + 61;
        for (int i = 0; i < 16; i++) {
            int shadowAlpha = aptitudeTicks < 40 ? Mth.clamp(aptitudeTicks * i / 13, 0, 12) : Mth.clamp(i, 0, 12);
            graphics.fill(
                    panelLeft + i,
                    panelTop + i,
                    panelRight - i,
                    panelBottom - i,
                    shadowAlpha << 24);
        }

        drawCenteredWithShadow(graphics, client, message, centerX, y, alpha, 0xFF5555);
        renderAptitudeRequirements(graphics, client, centerX, y + 15, alpha);
    }

    private static boolean shouldHideAptitudeWarning(Minecraft client) {
        if (client.player == null || client.player.isCreative() || aptitudeRequirements.isEmpty()) {
            return true;
        }

        PlayerProgress progress = PlayerProgressClientState.get().orElse(null);
        return progress != null && aptitudeRequirements.stream()
                .allMatch(requirement -> progress.getAptitudeLevel(requirement.getKey()) >= requirement.getAptitudeLvl());
    }

    private static void renderAptitudeRequirements(GuiGraphics graphics, Minecraft client, int centerX, int y, int alpha) {
        PlayerProgress progress = PlayerProgressClientState.get().orElse(null);
        int colorAlpha = alpha << 24;
        int count = aptitudeRequirements.size();
        for (int i = 0; i < count; i++) {
            Aptitudes requirement = aptitudeRequirements.get(i);
            int x = centerX + i * 24 - count * 12 + 4;
            int requiredLevel = requirement.getAptitudeLvl();
            boolean met = progress != null && progress.getAptitudeLevel(requirement.getAptitude()) >= requiredLevel;
            graphics.blit(
                    requirement.getAptitude().getLockedTexture(requiredLevel, CommonConfigService.aptitudeMaxLevel()),
                    x,
                    y,
                    0.0F,
                    0.0F,
                    16,
                    16,
                    16,
                    16);
            drawCenteredWithShadow(graphics, client, Component.literal(String.valueOf(requiredLevel)), x + 16, y + 12, alpha, met ? 0x55DD55 : 0xFF5555);
        }
    }

    private static void drawCenteredWithShadow(
            GuiGraphics graphics, Minecraft client, Component component, int centerX, int y, int alpha, int color) {
        int x = centerX - client.font.width(component) / 2;
        int argb = alpha << 24;
        graphics.drawString(client.font, component, x + 1, y + 1, argb, false);
        graphics.drawString(client.font, component, x, y, argb | color, false);
    }

    private static void playTitleSound() {
        Minecraft.getInstance()
                .getSoundManager()
                .play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(RegistrySounds.GAIN_TITLE, 1.0F));
    }
}
