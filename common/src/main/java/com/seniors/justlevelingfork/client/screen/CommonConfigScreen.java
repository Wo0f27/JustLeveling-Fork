package com.seniors.justlevelingfork.client.screen;

import com.seniors.justlevelingfork.client.core.SortPassives;
import com.seniors.justlevelingfork.client.core.SortSkills;
import com.seniors.justlevelingfork.common.config.ClientConfigService;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class CommonConfigScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int LABEL_WIDTH = 190;
    private static final int FIELD_WIDTH = 86;

    private final Screen parent;
    private final ConfigSaver saver;
    private final List<Row> rows = new ArrayList<>();
    private int scrollOffset;

    public CommonConfigScreen(Screen parent, ConfigSaver saver) {
        super(Component.translatable("justlevelingfork.config.title"));
        this.parent = parent;
        this.saver = saver;
    }

    @Override
    protected void init() {
        rows.clear();
        clearWidgets();
        int centerX = width / 2;
        int top = 44;
        int labelX = centerX - 150;
        int controlX = centerX + 58;

        addIntRow(labelX, controlX, top, "aptitudeMaxLevel", CommonConfigService.aptitudeMaxLevel(), 2, saver::setAptitudeMaxLevel);
        addIntRow(labelX, controlX, top, "playersMaxGlobalLevel", CommonConfigService.playersMaxGlobalLevel(), 32, saver::setPlayersMaxGlobalLevel);
        addIntRow(labelX, controlX, top, "aptitudeFirstCostLevel", CommonConfigService.aptitudeFirstCostLevel(), 1, saver::setAptitudeFirstCostLevel);
        addBooleanRow(labelX, controlX, top, "showPotionsHud", CommonConfigService.showPotionsHud(), saver::setShowPotionsHud);
        addBooleanRow(
                labelX,
                controlX,
                top,
                "showLuckyDropSkillOverlay",
                ClientConfigService.shouldShowSkillMessage("overlay.skill.justlevelingfork.lucky_drop"),
                saver::setShowLuckyDropSkillOverlay);
        addBooleanRow(
                labelX,
                controlX,
                top,
                "showCriticalRollSkillOverlay",
                ClientConfigService.shouldShowSkillMessage("overlay.skill.justlevelingfork.critical_roll_1"),
                saver::setShowCriticalRollSkillOverlay);
        addBooleanRow(labelX, controlX, top, "showSkillModName", ClientConfigService.showSkillModName(), saver::setShowSkillModName);
        addBooleanRow(labelX, controlX, top, "showTitleModName", ClientConfigService.showTitleModName(), saver::setShowTitleModName);
        addEnumRow(labelX, controlX, top, "sortPassive", ClientConfigService.passiveSort(), SortPassives.values(), saver::setPassiveSort);
        addEnumRow(labelX, controlX, top, "sortSkill", ClientConfigService.skillSort(), SortSkills.values(), saver::setSkillSort);
        addBooleanRow(labelX, controlX, top, "dropLockedItems", CommonConfigService.dropLockedItems(), saver::setDropLockedItems);
        addBooleanRow(labelX, controlX, top, "hideMetUsageRequirements", CommonConfigService.hideMetUsageRequirements(), saver::setHideMetUsageRequirements);
        addBooleanRow(labelX, controlX, top, "skillResetRefundsSpentLevels", CommonConfigService.skillResetRefundsSpentLevels(), saver::setSkillResetRefundsSpentLevels);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(centerX - 102, height - 28, 98, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), button -> resetScroll())
                .bounds(centerX + 4, height - 28, 98, 20)
                .tooltip(Tooltip.create(Component.translatable("justlevelingfork.config.reset_scroll")))
                .build());
        updateRowPositions();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        graphics.drawCenteredString(
                font,
                Component.translatable("justlevelingfork.config.restart_notice").withStyle(ChatFormatting.GRAY),
                width / 2,
                28,
                0xA0A0A0);
        for (Row row : rows) {
            if (row.control.visible) {
                graphics.drawString(font, row.label, row.labelX, row.control.getY() + 6, 0xE0E0E0, false);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, rows.size() * ROW_HEIGHT - (height - 86));
        scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta) * ROW_HEIGHT, 0, maxScroll);
        updateRowPositions();
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private void addIntRow(int labelX, int controlX, int top, String key, int value, int min, IntConsumer setter) {
        EditBox editBox = new EditBox(font, controlX, top, FIELD_WIDTH, 20, label(key));
        editBox.setValue(Integer.toString(value));
        editBox.setFilter(text -> text.isEmpty() || text.matches("\\d{0,6}"));
        editBox.setResponder(text -> {
            if (!text.isEmpty()) {
                setter.accept(Math.max(min, Integer.parseInt(text)));
            }
        });
        editBox.setTooltip(Tooltip.create(description(key)));
        addRenderableWidget(editBox);
        rows.add(new Row(labelX, top, label(key), editBox));
    }

    private void addBooleanRow(int labelX, int controlX, int top, String key, boolean value, Consumer<Boolean> setter) {
        final boolean[] current = {value};
        Button button = Button.builder(booleanLabel(current[0]), clicked -> {
                    current[0] = !current[0];
                    setter.accept(current[0]);
                    clicked.setMessage(booleanLabel(current[0]));
                })
                .bounds(controlX, top, FIELD_WIDTH, 20)
                .tooltip(Tooltip.create(description(key)))
                .build();
        addRenderableWidget(button);
        rows.add(new Row(labelX, top, label(key), button));
    }

    private <T extends Enum<T>> void addEnumRow(int labelX, int controlX, int top, String key, T value, T[] values, Consumer<T> setter) {
        final int[] index = {Math.max(0, value.ordinal())};
        Button button = Button.builder(Component.literal(values[index[0]].name()), clicked -> {
                    index[0] = (index[0] + 1) % values.length;
                    setter.accept(values[index[0]]);
                    clicked.setMessage(Component.literal(values[index[0]].name()));
                })
                .bounds(controlX, top, FIELD_WIDTH, 20)
                .tooltip(Tooltip.create(description(key)))
                .build();
        addRenderableWidget(button);
        rows.add(new Row(labelX, top, label(key), button));
    }

    private void updateRowPositions() {
        int visibleTop = 44;
        int visibleBottom = height - 34;
        for (int index = 0; index < rows.size(); index++) {
            Row row = rows.get(index);
            int y = visibleTop + index * ROW_HEIGHT - scrollOffset;
            row.control.setY(y);
            row.control.visible = y >= visibleTop && y + 20 <= visibleBottom;
        }
    }

    private void resetScroll() {
        scrollOffset = 0;
        updateRowPositions();
    }

    private Component label(String key) {
        return Component.translatable("yacl3.config.justlevelingfork:config." + key);
    }

    private Component description(String key) {
        return Component.translatable("yacl3.config.justlevelingfork:config." + key + ".desc");
    }

    private Component booleanLabel(boolean value) {
        return Component.translatable(value ? "options.on" : "options.off");
    }

    private record Row(int labelX, int baseY, Component label, net.minecraft.client.gui.components.AbstractWidget control) {
    }

    public interface ConfigSaver {
        void setAptitudeMaxLevel(int value);

        void setPlayersMaxGlobalLevel(int value);

        void setAptitudeFirstCostLevel(int value);

        void setShowPotionsHud(boolean value);

        void setShowLuckyDropSkillOverlay(boolean value);

        void setShowCriticalRollSkillOverlay(boolean value);

        void setShowSkillModName(boolean value);

        void setShowTitleModName(boolean value);

        void setPassiveSort(SortPassives value);

        void setSkillSort(SortSkills value);

        void setDropLockedItems(boolean value);

        void setHideMetUsageRequirements(boolean value);

        void setSkillResetRefundsSpentLevels(boolean value);
    }
}
