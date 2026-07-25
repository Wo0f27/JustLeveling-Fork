package com.seniors.justlevelingfork.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seniors.justlevelingfork.client.core.SortPassives;
import com.seniors.justlevelingfork.client.core.SortSkills;
import com.seniors.justlevelingfork.client.gui.ClientTabs;
import com.seniors.justlevelingfork.common.config.ClientConfigService;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.AptitudeExperience;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class AptitudesOverviewScreen extends Screen {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int FONT_COLOR = 0x3E3E3E;
    private static final int MUTED_COLOR = 0x666666;
    private static final int WHITE = 0xF0F0F0;
    private static final int GREEN = 0x55DD55;
    private static final int RED = 0xDD5555;
    private static final int DARK = 0x555555;
    private static final int PAGE_APTITUDES = 0;
    private static final int PAGE_SKILLS = 1;
    private static final int PAGE_TITLES = 2;
    private static final int SKILL_ROWS = 4;
    private static final int SKILL_ROW_SIZE = 5;
    private static final int SKILLS_PER_PAGE = SKILL_ROW_SIZE * SKILL_ROWS;
    private static final int SKILL_ROW_SPACING = 26;
    private static final int SKILL_ICON_SPACING = 26;
    private static final int SKILL_ROW_CENTER_X = 88;
    private static final int SKILL_GRID_CENTER_Y = 103;

    private int page = PAGE_APTITUDES;
    private String selectedAptitude = RegistryAptitudes.STRENGTH.getName();
    private int skillPage;
    private int titleScroll;
    private String titleSearchValue = "";
    private EditBox titleSearch;

    public AptitudesOverviewScreen() {
        super(Component.translatable("screen.aptitude.title"));
    }

    @Override
    protected void init() {
        int left = left();
        int top = top();
        titleSearch = new EditBox(this.font, left + 41, top + 17, 93, 12, Component.translatable("screen.title.search"));
        titleSearch.setMaxLength(50);
        titleSearch.setBordered(true);
        titleSearch.setTextColor(0xFFFFFF);
        titleSearch.setValue(titleSearchValue);
        addRenderableWidget(titleSearch);
        updateTitleSearchVisibility();
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        RenderSystem.enableBlend();
        int left = left();
        int top = top();
        if (page == PAGE_SKILLS) {
            Aptitude aptitude = selectedAptitude();
            graphics.blit(aptitude.background, left + 7, top + 30, 0.0F, 0.0F, 160, 128, 16, 16);
        }
        graphics.blit(HandlerResources.SKILL_PAGE[page], left, top, 0, 0, WIDTH, HEIGHT);

        PlayerProgress progress = PlayerProgressClientState.get().orElse(null);
        if (progress != null) {
            if (page == PAGE_APTITUDES) {
                renderAptitudes(graphics, progress, left, top, mouseX, mouseY);
            } else if (page == PAGE_SKILLS) {
                renderSkills(graphics, progress, left, top, mouseX, mouseY);
            } else {
                renderTitles(graphics, progress, left, top, mouseX, mouseY, partialTick);
            }
        }

        ClientTabs.render(graphics, this, mouseX, mouseY, WIDTH, HEIGHT);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderAptitudes(GuiGraphics graphics, PlayerProgress progress, int left, int top, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            drawCentered(graphics, client.player.getName(), left + 88, top + 7, FONT_COLOR);
            drawCentered(
                    graphics,
                    Component.translatable("screen.aptitude.level", client.player.experienceLevel, AptitudeExperience.getPlayerXP(client.player)),
                    left + 88,
                    top + 17,
                    FONT_COLOR);
        }

        Title title = RegistryTitles.getTitle(progress.playerTitle);
        if (title == null) {
            title = RegistryTitles.TITLELESS;
        }
        Component titleName = Component.translatable(title.getKey());
        int titleWidth = Math.min(130, font.width(titleName) + 17);
        int titleX = left + 88 - titleWidth / 2;
        boolean titleHover = isMouseWithin(titleX - 2, top + 27, mouseX, mouseY, titleWidth + 4, 14);
        graphics.blit(HandlerResources.SKILL_PAGE[0], titleX - 2, top + 27, titleHover ? 4 : 0, 214, 2, 14);
        graphics.blit(HandlerResources.SKILL_PAGE[0], titleX, top + 27, 0, titleHover ? 228 : 242, titleWidth, 14);
        graphics.blit(HandlerResources.SKILL_PAGE[0], titleX + titleWidth, top + 27, titleHover ? 6 : 2, 214, 2, 14);
        graphics.drawString(font, titleName, titleX + 2, top + 30, WHITE, false);
        graphics.blit(HandlerResources.SKILL_PAGE[0], titleX + titleWidth - 10, top + 30, 8, 218, 8, 8);
        if (titleHover) {
            graphics.renderTooltip(font, Component.translatable("screen.title.choose_your_title"), mouseX, mouseY);
        }

        int xpWidth = client.player == null ? 0 : (int) (client.player.experienceProgress * 151.0F);
        graphics.blit(HandlerResources.SKILL_PAGE[0], left + 12, top + 43, 0, 166, xpWidth, 5);

        List<Aptitude> aptitudes = sortedAptitudes();
        for (int i = 0; i < aptitudes.size(); i++) {
            Aptitude aptitude = aptitudes.get(i);
            int level = progress.getAptitudeLevel(aptitude);
            int x = left + 12 + i % 2 * 77;
            int y = top + 50 + i / 2 * 28;
            boolean hover = isMouseWithin(x, y, mouseX, mouseY, 74, 26);
            if (hover) {
                graphics.blit(HandlerResources.SKILL_PAGE[0], x, y, 176, 0, 73, 26);
            }
            graphics.blit(aptitude.getLockedTexture(level, CommonConfigService.aptitudeMaxLevel()), x + 5, y + 5, 0, 0, 16, 16, 16, 16);
            graphics.drawString(font, Component.translatable(aptitude.getKey() + ".abbreviation").withStyle(ChatFormatting.BOLD), x + 24, y + 5, WHITE, false);
            graphics.drawString(
                    font,
                    Component.translatable("screen.aptitude.experience", twoDigits(level), CommonConfigService.aptitudeMaxLevel()),
                    x + 24,
                    y + 14,
                    0xAAAAAA,
                    false);
            if (hover) {
                graphics.renderTooltip(font, Component.translatable(aptitude.getKey()), mouseX, mouseY);
            }
        }
    }

    private void renderSkills(GuiGraphics graphics, PlayerProgress progress, int left, int top, int mouseX, int mouseY) {
        Aptitude aptitude = selectedAptitude();
        int aptitudeLevel = progress.getAptitudeLevel(aptitude);
        graphics.blit(aptitude.getLockedTexture(aptitudeLevel, CommonConfigService.aptitudeMaxLevel()), left + 12, top + 9, 0, 0, 16, 16, 16, 16);
        graphics.drawString(font, Component.translatable(aptitude.getKey()).withStyle(ChatFormatting.BOLD), left + 34, top + 8, FONT_COLOR, false);
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.skill.level_and_rank",
                        twoDigits(aptitudeLevel),
                        CommonConfigService.aptitudeMaxLevel(),
                        aptitude.getRank(aptitudeLevel, CommonConfigService.aptitudeMaxLevel())),
                left + 34,
                top + 18,
                FONT_COLOR,
                false);

        renderLevelButton(graphics, progress, aptitude, left, top, mouseX, mouseY);

        List<Object> entries = skillEntries(aptitude);
        int totalPages = Math.max(1, (entries.size() + SKILLS_PER_PAGE - 1) / SKILLS_PER_PAGE);
        skillPage = Mth.clamp(skillPage, 0, totalPages - 1);
        int start = skillPage * SKILLS_PER_PAGE;
        int end = Math.min(entries.size(), start + SKILLS_PER_PAGE);
        for (int i = start; i < end; i++) {
            int local = i - start;
            int row = local / SKILL_ROW_SIZE;
            int col = local % SKILL_ROW_SIZE;
            int rowSize = Math.min(SKILL_ROW_SIZE, end - (start + row * SKILL_ROW_SIZE));
            int x = skillIconX(left, col, rowSize);
            int y = skillIconY(top, row, rowsOnPage(end - start));
            renderEntryIcon(graphics, progress, entries.get(i), x, y, mouseX, mouseY);
        }

        renderBackButton(graphics, left, top, mouseX, mouseY);
        if (totalPages > 1) {
            Component label = Component.literal((skillPage + 1) + "/" + totalPages);
            drawCentered(graphics, label, left + 88, top + 146, WHITE);
            graphics.blit(HandlerResources.SKILL_PAGE[1], left + 67, top + 144, 241, skillPage > 0 ? 12 : 0, 7, 11);
            graphics.blit(HandlerResources.SKILL_PAGE[1], left + 103, top + 144, 249, skillPage < totalPages - 1 ? 12 : 0, 7, 11);
        }
    }

    private void renderLevelButton(GuiGraphics graphics, PlayerProgress progress, Aptitude aptitude, int left, int top, int mouseX, int mouseY) {
        boolean canLevel = canLevelAptitude(progress, aptitude);
        int iconOffset = progress.getAptitudeLevel(aptitude) >= CommonConfigService.aptitudeMaxLevel() ? 12 : canLevel ? 6 : 0;
        graphics.blit(HandlerResources.SKILL_PAGE[1], left + 153, top + 14, 177 + iconOffset, 1, 6, 6);
        if (!isMouseWithin(left + 149, top + 10, mouseX, mouseY, 14, 14)) {
            return;
        }
        int aptitudeLevel = progress.getAptitudeLevel(aptitude);
        if (progress.getGlobalLevel() >= CommonConfigService.playersMaxGlobalLevel()) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("tooltip.aptitude.global_max_level", CommonConfigService.playersMaxGlobalLevel()).withStyle(ChatFormatting.RED),
                    mouseX,
                    mouseY);
        } else if (aptitudeLevel >= CommonConfigService.aptitudeMaxLevel()) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("tooltip.aptitude.max_level", Component.translatable(aptitude.getKey()).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY),
                    mouseX,
                    mouseY);
        } else {
            int firstCost = CommonConfigService.aptitudeFirstCostLevel();
            ChatFormatting color = canLevel ? ChatFormatting.GREEN : ChatFormatting.RED;
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "tooltip.aptitude.level_up",
                            Component.literal(String.valueOf(AptitudeExperience.requiredExperienceLevels(aptitudeLevel, firstCost))).withStyle(color),
                            Component.literal(String.valueOf(AptitudeExperience.requiredPoints(aptitudeLevel, firstCost))).withStyle(color),
                            Component.translatable(aptitude.getKey()).withStyle(color)).withStyle(ChatFormatting.GRAY),
                    mouseX,
                    mouseY);
        }
    }

    private void renderEntryIcon(GuiGraphics graphics, PlayerProgress progress, Object entry, int x, int y, int mouseX, int mouseY) {
        boolean hover = isMouseWithin(x, y, mouseX, mouseY, 24, 24);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (entry instanceof Passive passive) {
            int level = progress.getPassiveLevel(passive);
            int iconV = level == passive.getMaxLevel() ? 24 : 0;
            graphics.blit(passive.getTexture(), x + 2, y + 2, 0, 0, 20, 20, 20, 20);
            graphics.blit(HandlerResources.SKILL_ICONS, x, y, 0, iconV, 24, 24, 72, 72);
            if (hover) {
                boolean canDown = level > 0;
                boolean canUp = level < passive.getMaxLevel() && progress.getAptitudeLevel(passive.aptitude) >= passive.getNextLevelUp(level);
                int downV = canDown ? (isMouseWithin(x + 2, y + 2, mouseX, mouseY, 9, 9) ? 20 : 10) : 0;
                int upV = canUp ? (isMouseWithin(x + 13, y + 2, mouseX, mouseY, 9, 9) ? 20 : 10) : 0;
                renderTooltipList(graphics, passive.tooltip(), mouseX, mouseY);
                RenderSystem.enableBlend();
                graphics.blit(HandlerResources.SKILL_ICONS, x, y, 0, 48, 24, 24, 72, 72);
                graphics.blit(HandlerResources.SKILL_PAGE[1], x + 2, y + 2, 1, 167 + downV, 9, 9);
                graphics.blit(HandlerResources.SKILL_PAGE[1], x + 13, y + 2, 11, 167 + upV, 9, 9);
            }
            renderPassiveLevel(graphics, level, x, y);
        } else if (entry instanceof Skill skill) {
            boolean available = skill.canToggleAtLevel(progress.getAptitudeLevel(skill.aptitude));
            graphics.blit(skill.getTexture(), x + 2, y + 2, 0, 0, 20, 20, 20, 20);
            graphics.blit(HandlerResources.SKILL_ICONS, x, y, 24, available ? 24 : 0, 24, 24, 72, 72);
            if (!progress.getToggleSkill(skill)) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.blit(HandlerResources.SKILL_ICONS, x, y, 24, 48, 24, 24, 72, 72);
            }
            if (hover) {
                renderTooltipList(graphics, skill.tooltip(), mouseX, mouseY);
            }
        }
    }

    private void renderTitles(GuiGraphics graphics, PlayerProgress progress, int left, int top, int mouseX, int mouseY, float partialTick) {
        drawCentered(graphics, Component.translatable("screen.title.choose_your_title"), left + 88, top + 7, FONT_COLOR);
        titleSearch.render(graphics, mouseX, mouseY, partialTick);
        List<Title> titles = visibleTitles(progress);
        int maxRows = 9;
        titleScroll = Mth.clamp(titleScroll, 0, Math.max(0, titles.size() - maxRows));
        int rows = Math.min(maxRows, titles.size() - titleScroll);
        for (int i = 0; i < rows; i++) {
            Title title = titles.get(titleScroll + i);
            boolean unlocked = progress.getLockTitle(title);
            boolean selected = title.getName().equals(progress.playerTitle);
            int y = top + 34 + i * 12;
            int color = unlocked ? selected ? GREEN : WHITE : DARK;
            if (isMouseWithin(left + 8, y - 1, mouseX, mouseY, 142, 10)) {
                graphics.blit(HandlerResources.SKILL_PAGE[2], left + 8, y - 1, 0, 166, 142, 10);
                renderTooltipList(graphics, title.tooltip(), mouseX, mouseY);
            }
            graphics.drawString(font, Component.translatable(title.getKey()), left + 10, y, color, false);
        }
        if (titles.size() > maxRows) {
            int scrollbarY = top + 33 + Math.round(91.0F / Math.max(1, titles.size() - maxRows) * titleScroll);
            graphics.blit(HandlerResources.SKILL_PAGE[2], left + 156, scrollbarY, 176, 0, 12, 15);
        }
        renderBackButton(graphics, left, top, mouseX, mouseY);
    }

    private void renderBackButton(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        int x = left + 141;
        int y = top + 144;
        boolean hover = isMouseWithin(x, y, mouseX, mouseY, 18, 10);
        graphics.blit(HandlerResources.SKILL_PAGE[page], x, y, hover ? 222 : 204, 0, 18, 10);
        if (hover) {
            graphics.renderTooltip(font, Component.translatable(page == PAGE_TITLES ? "tooltip.title.back" : "tooltip.skill.back"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ClientTabs.mouseClicked(this, mouseX, mouseY, button, WIDTH, HEIGHT)) {
            return true;
        }
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        PlayerProgress progress = PlayerProgressClientState.get().orElse(null);
        if (progress == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int left = left();
        int top = top();
        if (page == PAGE_APTITUDES && handleAptitudeClick(progress, left, top, mouseX, mouseY)) {
            return true;
        }
        if (page == PAGE_SKILLS && handleSkillClick(progress, left, top, mouseX, mouseY)) {
            return true;
        }
        if (page == PAGE_TITLES && handleTitleClick(progress, left, top, mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleAptitudeClick(PlayerProgress progress, int left, int top, double mouseX, double mouseY) {
        if (isMouseWithin(left + 23, top + 27, mouseX, mouseY, 130, 14)) {
            page = PAGE_TITLES;
            updateTitleSearchVisibility();
            return true;
        }
        List<Aptitude> aptitudes = sortedAptitudes();
        for (int i = 0; i < aptitudes.size(); i++) {
            int x = left + 12 + i % 2 * 77;
            int y = top + 50 + i / 2 * 28;
            if (isMouseWithin(x, y, mouseX, mouseY, 74, 26)) {
                selectedAptitude = aptitudes.get(i).getName();
                skillPage = 0;
                page = PAGE_SKILLS;
                updateTitleSearchVisibility();
                return true;
            }
        }
        return false;
    }

    private boolean handleSkillClick(PlayerProgress progress, int left, int top, double mouseX, double mouseY) {
        Aptitude aptitude = selectedAptitude();
        if (isMouseWithin(left + 149, top + 10, mouseX, mouseY, 14, 14) && canLevelAptitude(progress, aptitude)) {
            PlayerProgressClientRequests.requestAptitudeLevelUp(aptitude);
            return true;
        }
        if (isMouseWithin(left + 141, top + 144, mouseX, mouseY, 18, 10)) {
            page = PAGE_APTITUDES;
            updateTitleSearchVisibility();
            return true;
        }
        List<Object> entries = skillEntries(aptitude);
        int totalPages = Math.max(1, (entries.size() + SKILLS_PER_PAGE - 1) / SKILLS_PER_PAGE);
        if (totalPages > 1) {
            if (isMouseWithin(left + 67, top + 144, mouseX, mouseY, 7, 11) && skillPage > 0) {
                skillPage--;
                return true;
            }
            if (isMouseWithin(left + 103, top + 144, mouseX, mouseY, 7, 11) && skillPage < totalPages - 1) {
                skillPage++;
                return true;
            }
        }
        int start = skillPage * SKILLS_PER_PAGE;
        int end = Math.min(entries.size(), start + SKILLS_PER_PAGE);
        for (int i = start; i < end; i++) {
            int local = i - start;
            int row = local / SKILL_ROW_SIZE;
            int col = local % SKILL_ROW_SIZE;
            int rowSize = Math.min(SKILL_ROW_SIZE, end - (start + row * SKILL_ROW_SIZE));
            int x = skillIconX(left, col, rowSize);
            int y = skillIconY(top, row, rowsOnPage(end - start));
            Object entry = entries.get(i);
            if (entry instanceof Passive passive && isMouseWithin(x, y, mouseX, mouseY, 24, 24)) {
                int level = progress.getPassiveLevel(passive);
                if (isMouseWithin(x + 2, y + 2, mouseX, mouseY, 9, 9) && level > 0) {
                    PlayerProgressClientRequests.requestPassiveLevelDown(passive);
                    return true;
                }
                if (isMouseWithin(x + 13, y + 2, mouseX, mouseY, 9, 9)
                        && level < passive.getMaxLevel()
                        && progress.getAptitudeLevel(passive.aptitude) >= passive.getNextLevelUp(level)) {
                    PlayerProgressClientRequests.requestPassiveLevelUp(passive);
                    return true;
                }
            } else if (entry instanceof Skill skill
                    && isMouseWithin(x, y, mouseX, mouseY, 24, 24)
                    && skill.canToggleAtLevel(progress.getAptitudeLevel(skill.aptitude))) {
                PlayerProgressClientRequests.requestSetToggleSkill(skill, !progress.getToggleSkill(skill));
                return true;
            }
        }
        return false;
    }

    private boolean handleTitleClick(PlayerProgress progress, int left, int top, double mouseX, double mouseY) {
        if (isMouseWithin(left + 141, top + 144, mouseX, mouseY, 18, 10)) {
            page = PAGE_APTITUDES;
            updateTitleSearchVisibility();
            return true;
        }
        List<Title> titles = visibleTitles(progress);
        int rows = Math.min(9, titles.size() - titleScroll);
        for (int i = 0; i < rows; i++) {
            if (isMouseWithin(left + 8, top + 33 + i * 12, mouseX, mouseY, 142, 10)) {
                Title title = titles.get(titleScroll + i);
                if (progress.getLockTitle(title)) {
                    PlayerProgressClientRequests.requestSetPlayerTitle(title);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (page == PAGE_TITLES) {
            titleScroll = Mth.clamp(titleScroll - (int) Math.signum(amount), 0, Math.max(0, visibleTitles(PlayerProgressClientState.get().orElse(null)).size() - 9));
            return true;
        }
        if (page == PAGE_SKILLS) {
            int totalPages = Math.max(1, (skillEntries(selectedAptitude()).size() + SKILLS_PER_PAGE - 1) / SKILLS_PER_PAGE);
            skillPage = Mth.clamp(skillPage - (int) Math.signum(amount), 0, totalPages - 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (page == PAGE_TITLES && titleSearch.charTyped(codePoint, modifiers)) {
            titleSearchValue = titleSearch.getValue();
            titleScroll = 0;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (page == PAGE_TITLES && titleSearch.keyPressed(keyCode, scanCode, modifiers)) {
            titleSearchValue = titleSearch.getValue();
            titleScroll = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<Object> skillEntries(Aptitude aptitude) {
        List<Passive> passives = new ArrayList<>(RegistryPassives.values().stream()
                .filter(passive -> passive.aptitude == aptitude)
                .toList());
        if (ClientConfigService.passiveSort() == SortPassives.ByReverseName) {
            passives.sort(Comparator.comparing(Passive::getName).reversed());
        } else {
            passives.sort(Comparator.comparing(Passive::getName));
        }

        List<Skill> skills = new ArrayList<>(RegistrySkills.values().stream()
                .filter(skill -> skill.aptitude == aptitude)
                .toList());
        if (ClientConfigService.skillSort() == SortSkills.ByReverseName) {
            skills.sort(Comparator.comparing(Skill::getName).reversed());
        } else if (ClientConfigService.skillSort() == SortSkills.ByName) {
            skills.sort(Comparator.comparing(Skill::getName));
        } else {
            skills.sort(Comparator.comparingInt(Skill::getLvl).thenComparing(Skill::getName));
        }

        List<Object> entries = new ArrayList<>();
        entries.addAll(passives);
        entries.addAll(skills);
        return entries;
    }

    private int rowsOnPage(int entryCount) {
        return Math.max(1, (entryCount + SKILL_ROW_SIZE - 1) / SKILL_ROW_SIZE);
    }

    private int skillIconX(int left, int column, int rowSize) {
        return left + SKILL_ROW_CENTER_X - 12 + SKILL_ICON_SPACING * column - 13 * (rowSize - 1);
    }

    private int skillIconY(int top, int row, int rowsOnPage) {
        return top + SKILL_GRID_CENTER_Y - 12 + SKILL_ROW_SPACING * row - rowsOnPage * 13;
    }

    private void renderPassiveLevel(GuiGraphics graphics, int level, int x, int y) {
        String value = String.valueOf(level);
        int labelX = x + 9 - font.width(value) / 2;
        graphics.blit(HandlerResources.SKILL_PAGE[1], labelX, y + 17, 21, 167, 7, 8);
        graphics.drawString(font, value, labelX + 8, y + 18, 0x000000, false);
        graphics.drawString(font, value, labelX + 7, y + 17, WHITE, false);
    }

    private List<Title> visibleTitles(PlayerProgress progress) {
        String search = titleSearch == null ? titleSearchValue : titleSearch.getValue();
        String normalizedSearch = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        List<Title> unlocked = new ArrayList<>();
        List<Title> locked = new ArrayList<>();
        for (Title title : RegistryTitles.defaults()) {
            boolean titleUnlocked = progress != null && progress.getLockTitle(title);
            if (!titleUnlocked && title.HideRequirements) {
                continue;
            }
            String translated = Component.translatable(title.getKey()).getString().toLowerCase(Locale.ROOT);
            if (!normalizedSearch.isEmpty() && !translated.contains(normalizedSearch)) {
                continue;
            }
            if (titleUnlocked) {
                unlocked.add(title);
            } else {
                locked.add(title);
            }
        }
        Comparator<Title> byName = Comparator.comparing(title -> Component.translatable(title.getKey()).getString());
        unlocked.sort(byName);
        locked.sort(byName);
        unlocked.addAll(locked);
        return unlocked;
    }

    private List<Aptitude> sortedAptitudes() {
        return RegistryAptitudes.values().stream().sorted(Comparator.comparingInt(aptitude -> aptitude.index)).toList();
    }

    private Aptitude selectedAptitude() {
        Aptitude aptitude = RegistryAptitudes.getAptitude(selectedAptitude);
        return aptitude == null ? RegistryAptitudes.STRENGTH : aptitude;
    }

    private void updateTitleSearchVisibility() {
        if (titleSearch != null) {
            titleSearch.setVisible(page == PAGE_TITLES);
            titleSearch.setFocused(page == PAGE_TITLES);
        }
    }

    private int left() {
        return (width - WIDTH) / 2;
    }

    private int top() {
        return (height - HEIGHT) / 2;
    }

    private void drawCentered(GuiGraphics graphics, Component component, int x, int y, int color) {
        graphics.drawString(font, component, x - font.width(component) / 2, y, color, false);
    }

    private void renderTooltipList(GuiGraphics graphics, List<Component> lines, int mouseX, int mouseY) {
        List<FormattedCharSequence> orderedLines = lines.stream()
                .map(Component::getVisualOrderText)
                .toList();
        graphics.renderTooltip(font, orderedLines, mouseX, mouseY);
    }

    private static String twoDigits(int value) {
        return String.format(Locale.ROOT, "%02d", value);
    }

    private static boolean canLevelAptitude(PlayerProgress progress, Aptitude aptitude) {
        return progress.getAptitudeLevel(aptitude) < CommonConfigService.aptitudeMaxLevel()
                && progress.getGlobalLevel() < CommonConfigService.playersMaxGlobalLevel();
    }

    private static boolean isMouseWithin(int x, int y, double mouseX, double mouseY, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
