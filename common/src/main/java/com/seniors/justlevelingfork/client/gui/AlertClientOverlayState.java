package com.seniors.justlevelingfork.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Client-only presentation state for the Alert feat warning.
 *
 * The server decides when a hostile has been newly detected and sends only
 * the number of new hostiles for presentation here.
 */
public final class AlertClientOverlayState {

    private static final int ALERT_TICKS = 40;

    private static int alertTicks;
    private static int hostileCount;

    private AlertClientOverlayState() {
    }

    public static void show(int count) {
        hostileCount = Math.max(1, count);
        alertTicks = ALERT_TICKS;
    }

    public static void tick() {
        if (alertTicks > 0) {
            alertTicks--;
        }
    }

    public static void clear() {
        alertTicks = 0;
        hostileCount = 0;
    }

    public static void render(GuiGraphics graphics) {
        if (graphics == null || alertTicks <= 0) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        Component message = hostileCount <= 1
                ? Component.literal("HOSTILE NEARBY").withStyle(ChatFormatting.BOLD)
                : Component.literal(hostileCount + " HOSTILES NEARBY").withStyle(ChatFormatting.BOLD);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;

        // Positioned above the hotbar, hearts, armor, and hunger bars.
        int y = screenHeight - 68;

        int alpha = alertTicks < 10
                ? Mth.clamp(alertTicks * 25, 0, 255)
                : 255;

        int textWidth = client.font.width(message);
        int panelLeft = centerX - textWidth / 2 - 9;
        int panelRight = centerX + textWidth / 2 + 9;
        int panelTop = y - 5;
        int panelBottom = y + 14;

        int backgroundAlpha = Math.min(170, alpha);
        graphics.fill(
                panelLeft,
                panelTop,
                panelRight,
                panelBottom,
                (backgroundAlpha << 24) | 0x220000);

        graphics.fill(
                panelLeft,
                panelTop,
                panelLeft + 2,
                panelBottom,
                (alpha << 24) | 0xFF5555);

        graphics.fill(
                panelRight - 2,
                panelTop,
                panelRight,
                panelBottom,
                (alpha << 24) | 0xFF5555);

        graphics.drawCenteredString(
                client.font,
                message,
                centerX,
                y,
                (alpha << 24) | 0xFF5555);
    }
}
