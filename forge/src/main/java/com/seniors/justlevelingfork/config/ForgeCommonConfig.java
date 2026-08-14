package com.seniors.justlevelingfork.config;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.client.core.SortPassives;
import com.seniors.justlevelingfork.client.core.SortSkills;
import com.seniors.justlevelingfork.common.config.ClientConfigService;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.integration.ForgeIntegrationConfig;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class ForgeCommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue APTITUDE_MAX_LEVEL;
    private static final ForgeConfigSpec.IntValue PLAYERS_MAX_GLOBAL_LEVEL;
    private static final ForgeConfigSpec.IntValue APTITUDE_FIRST_COST_LEVEL;
    private static final ForgeConfigSpec.BooleanValue SHOW_POTIONS_HUD;
    private static final ForgeConfigSpec.BooleanValue SHOW_LUCKY_DROP_SKILL_OVERLAY;
    private static final ForgeConfigSpec.BooleanValue SHOW_CRITICAL_ROLL_SKILL_OVERLAY;
    private static final ForgeConfigSpec.BooleanValue SHOW_SKILL_MOD_NAME;
    private static final ForgeConfigSpec.BooleanValue SHOW_TITLE_MOD_NAME;
    private static final ForgeConfigSpec.EnumValue<SortPassives> SORT_PASSIVE;
    private static final ForgeConfigSpec.EnumValue<SortSkills> SORT_SKILL;
    private static final ForgeConfigSpec.BooleanValue DROP_LOCKED_ITEMS;
    private static final ForgeConfigSpec.BooleanValue ALLOW_LOCKED_ITEMS_IN_INVENTORY;
    private static final ForgeConfigSpec.BooleanValue HIDE_MET_USAGE_REQUIREMENTS;
    private static final ForgeConfigSpec.BooleanValue SKILL_RESET_REFUNDS_SPENT_LEVELS;
    private static final ForgeConfigSpec.BooleanValue LOG_TACZ_GUN_NAMES;
    private static final ForgeConfigSpec.BooleanValue LOG_SPELL_IDS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TREASURE_HUNTER_ITEMS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONVERGENCE_ITEMS;

    static {
        BUILDER.push("general");
        APTITUDE_MAX_LEVEL = BUILDER
                .comment("Aptitudes max level.")
                .defineInRange("aptitudeMaxLevel", 30, 2, 30);
        PLAYERS_MAX_GLOBAL_LEVEL = BUILDER
                .comment("Global max level, calculated by summing all aptitude levels.")
                .defineInRange("playersMaxGlobalLevel", 256, 32, 99999);
        APTITUDE_FIRST_COST_LEVEL = BUILDER
                .comment("First aptitudes level cost.")
                .defineInRange("aptitudeFirstCostLevel", 5, 1, 1000);
        SHOW_POTIONS_HUD = BUILDER
                .comment("Show potions overlay over skills.")
                .define("showPotionsHud", true);
        SHOW_LUCKY_DROP_SKILL_OVERLAY = BUILDER
                .comment("Show Lucky Drop actionbar skill messages.")
                .define("showLuckyDropSkillOverlay", true);
        SHOW_CRITICAL_ROLL_SKILL_OVERLAY = BUILDER
                .comment("Show Critical Roll actionbar skill messages.")
                .define("showCriticalRollSkillOverlay", true);
        SHOW_SKILL_MOD_NAME = BUILDER
                .comment("Show the source mod id on skill and passive tooltips.")
                .define("showSkillModName", false);
        SHOW_TITLE_MOD_NAME = BUILDER
                .comment("Show the source mod id on title tooltips.")
                .define("showTitleModName", false);
        SORT_PASSIVE = BUILDER
                .comment("Default passive sort order used by client screens.")
                .defineEnum("sortPassive", SortPassives.ByName);
        SORT_SKILL = BUILDER
                .comment("Default skill sort order used by client screens.")
                .defineEnum("sortSkill", SortSkills.ByLevel);
        DROP_LOCKED_ITEMS = BUILDER
                .comment("If true, locked items will be automatically dropped from player hands.")
                .define("dropLockedItems", false);
        ALLOW_LOCKED_ITEMS_IN_INVENTORY = BUILDER
                .comment(
                        "If true, locked items can remain in inventories and equipment slots.",
                        "Their aptitude requirements still prevent using them.",
                        "This overrides dropLockedItems and per-item <droppable> markers.")
                .define("allowLockedItemsInInventory", false);
        HIDE_MET_USAGE_REQUIREMENTS = BUILDER
                .comment("If true, item usage requirement tooltips hide requirements the player already meets.")
                .define("hideMetUsageRequirements", false);
        SKILL_RESET_REFUNDS_SPENT_LEVELS = BUILDER
                .comment("If true, using a Skill Reset Crystal refunds the experience spent on aptitude levels.")
                .define("skillResetRefundsSpentLevels", false);
        LOG_TACZ_GUN_NAMES = BUILDER
                .comment("If Timeless and Classics: Zero is present, log gun ids on fire for lock-item configuration.")
                .define("logTaczGunNames", false);
        LOG_SPELL_IDS = BUILDER
                .comment("If Iron's Spells 'n Spellbooks is present, log spell ids on cast for lock-item configuration.")
                .define("logSpellIds", false);
        TREASURE_HUNTER_ITEMS = BUILDER
                .comment("Treasure Hunter possible drops. Prefix lists with any label and wrap entries in [].")
                .defineListAllowEmpty(
                        "treasureHunterItemList",
                        CommonConfigService.defaultTreasureHunterItems(),
                        value -> value instanceof String string && !string.isBlank());
        CONVERGENCE_ITEMS = BUILDER
                .comment("Convergence crafting refunds in the format crafted_item#refund_item.")
                .defineListAllowEmpty(
                        "convergenceItemList",
                        CommonConfigService.defaultConvergenceItems(),
                        value -> value instanceof String string && string.contains("#"));
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ForgeCommonConfig() {
    }

    public static void load() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, Constants.CONFIG_NAME + ".toml");
        CommonConfigService.setAptitudeMaxLevel(APTITUDE_MAX_LEVEL::get);
        CommonConfigService.setPlayersMaxGlobalLevel(PLAYERS_MAX_GLOBAL_LEVEL::get);
        CommonConfigService.setAptitudeFirstCostLevel(APTITUDE_FIRST_COST_LEVEL::get);
        CommonConfigService.setShowPotionsHud(SHOW_POTIONS_HUD::get);
        ClientConfigService.setShowLuckyDropSkillOverlay(SHOW_LUCKY_DROP_SKILL_OVERLAY::get);
        ClientConfigService.setShowCriticalRollSkillOverlay(SHOW_CRITICAL_ROLL_SKILL_OVERLAY::get);
        ClientConfigService.setShowSkillModName(SHOW_SKILL_MOD_NAME::get);
        ClientConfigService.setShowTitleModName(SHOW_TITLE_MOD_NAME::get);
        ClientConfigService.setPassiveSort(SORT_PASSIVE::get);
        ClientConfigService.setSkillSort(SORT_SKILL::get);
        CommonConfigService.setDropLockedItems(DROP_LOCKED_ITEMS::get);
        CommonConfigService.setAllowLockedItemsInInventory(ALLOW_LOCKED_ITEMS_IN_INVENTORY::get);
        CommonConfigService.setHideMetUsageRequirements(HIDE_MET_USAGE_REQUIREMENTS::get);
        CommonConfigService.setSkillResetRefundsSpentLevels(SKILL_RESET_REFUNDS_SPENT_LEVELS::get);
        ForgeIntegrationConfig.setLogTaczGunNames(LOG_TACZ_GUN_NAMES::get);
        ForgeIntegrationConfig.setLogSpellIds(LOG_SPELL_IDS::get);
        CommonConfigService.setTreasureHunterItems(() -> List.copyOf(TREASURE_HUNTER_ITEMS.get()));
        CommonConfigService.setConvergenceItems(() -> List.copyOf(CONVERGENCE_ITEMS.get()));
    }

    public static void setAptitudeMaxLevel(int level) {
        APTITUDE_MAX_LEVEL.set(Math.min(CommonConfigService.MAX_APTITUDE_LEVEL, Math.max(2, level)));
        SPEC.save();
    }

    public static void setPlayersMaxGlobalLevel(int level) {
        PLAYERS_MAX_GLOBAL_LEVEL.set(Math.min(CommonConfigService.MAX_GLOBAL_LEVEL, Math.max(32, level)));
        SPEC.save();
    }

    public static void setAptitudeFirstCostLevel(int level) {
        APTITUDE_FIRST_COST_LEVEL.set(
                Math.min(CommonConfigService.MAX_FIRST_COST_LEVEL, Math.max(1, level)));
        SPEC.save();
    }

    public static void setShowPotionsHud(boolean value) {
        SHOW_POTIONS_HUD.set(value);
        SPEC.save();
    }

    public static void setShowLuckyDropSkillOverlay(boolean value) {
        SHOW_LUCKY_DROP_SKILL_OVERLAY.set(value);
        SPEC.save();
    }

    public static void setShowCriticalRollSkillOverlay(boolean value) {
        SHOW_CRITICAL_ROLL_SKILL_OVERLAY.set(value);
        SPEC.save();
    }

    public static void setShowSkillModName(boolean value) {
        SHOW_SKILL_MOD_NAME.set(value);
        SPEC.save();
    }

    public static void setShowTitleModName(boolean value) {
        SHOW_TITLE_MOD_NAME.set(value);
        SPEC.save();
    }

    public static void setPassiveSort(SortPassives value) {
        SORT_PASSIVE.set(value);
        SPEC.save();
    }

    public static void setSkillSort(SortSkills value) {
        SORT_SKILL.set(value);
        SPEC.save();
    }

    public static void setDropLockedItems(boolean value) {
        DROP_LOCKED_ITEMS.set(value);
        SPEC.save();
    }

    public static void setAllowLockedItemsInInventory(boolean value) {
        ALLOW_LOCKED_ITEMS_IN_INVENTORY.set(value);
        SPEC.save();
    }

    public static void setHideMetUsageRequirements(boolean value) {
        HIDE_MET_USAGE_REQUIREMENTS.set(value);
        SPEC.save();
    }

    public static void setSkillResetRefundsSpentLevels(boolean value) {
        SKILL_RESET_REFUNDS_SPENT_LEVELS.set(value);
        SPEC.save();
    }
}
