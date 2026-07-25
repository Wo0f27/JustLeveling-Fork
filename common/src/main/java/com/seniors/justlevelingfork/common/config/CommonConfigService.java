package com.seniors.justlevelingfork.common.config;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.List;

public final class CommonConfigService {
    public static final int MAX_APTITUDE_LEVEL = 1000;
    public static final int MAX_GLOBAL_LEVEL = 99999;
    public static final int MAX_FIRST_COST_LEVEL = 1000;
    private static IntSupplier aptitudeMaxLevel = () -> 32;
    private static IntSupplier playersMaxGlobalLevel = () -> 256;
    private static IntSupplier aptitudeFirstCostLevel = () -> 5;
    private static BooleanSupplier showPotionsHud = () -> true;
    private static BooleanSupplier dropLockedItems = () -> false;
    private static BooleanSupplier hideMetUsageRequirements = () -> false;
    private static BooleanSupplier skillResetRefundsSpentLevels = () -> false;
    private static Supplier<List<String>> treasureHunterItems = CommonConfigService::defaultTreasureHunterItems;
    private static Supplier<List<String>> convergenceItems = CommonConfigService::defaultConvergenceItems;

    private CommonConfigService() {
    }

    public static void setAptitudeFirstCostLevel(IntSupplier aptitudeFirstCostLevel) {
        CommonConfigService.aptitudeFirstCostLevel =
                Optional.ofNullable(aptitudeFirstCostLevel).orElse(() -> 5);
    }

    public static void setAptitudeMaxLevel(IntSupplier aptitudeMaxLevel) {
        CommonConfigService.aptitudeMaxLevel = Optional.ofNullable(aptitudeMaxLevel).orElse(() -> 32);
    }

    public static int aptitudeMaxLevel() {
        return Math.min(MAX_APTITUDE_LEVEL, Math.max(2, aptitudeMaxLevel.getAsInt()));
    }

    public static void setPlayersMaxGlobalLevel(IntSupplier playersMaxGlobalLevel) {
        CommonConfigService.playersMaxGlobalLevel = Optional.ofNullable(playersMaxGlobalLevel).orElse(() -> 256);
    }

    public static int playersMaxGlobalLevel() {
        return Math.min(MAX_GLOBAL_LEVEL, Math.max(32, playersMaxGlobalLevel.getAsInt()));
    }

    public static int aptitudeFirstCostLevel() {
        return Math.min(MAX_FIRST_COST_LEVEL, Math.max(1, aptitudeFirstCostLevel.getAsInt()));
    }

    public static void setShowPotionsHud(BooleanSupplier showPotionsHud) {
        CommonConfigService.showPotionsHud = Optional.ofNullable(showPotionsHud).orElse(() -> true);
    }

    public static boolean showPotionsHud() {
        return showPotionsHud.getAsBoolean();
    }

    public static void setDropLockedItems(BooleanSupplier dropLockedItems) {
        CommonConfigService.dropLockedItems = Optional.ofNullable(dropLockedItems).orElse(() -> false);
    }

    public static boolean dropLockedItems() {
        return dropLockedItems.getAsBoolean();
    }

    public static void setHideMetUsageRequirements(BooleanSupplier hideMetUsageRequirements) {
        CommonConfigService.hideMetUsageRequirements =
                Optional.ofNullable(hideMetUsageRequirements).orElse(() -> false);
    }

    public static boolean hideMetUsageRequirements() {
        return hideMetUsageRequirements.getAsBoolean();
    }

    public static void setSkillResetRefundsSpentLevels(BooleanSupplier skillResetRefundsSpentLevels) {
        CommonConfigService.skillResetRefundsSpentLevels =
                Optional.ofNullable(skillResetRefundsSpentLevels).orElse(() -> false);
    }

    public static boolean skillResetRefundsSpentLevels() {
        return skillResetRefundsSpentLevels.getAsBoolean();
    }

    public static void setTreasureHunterItems(Supplier<List<String>> treasureHunterItems) {
        CommonConfigService.treasureHunterItems =
                Optional.ofNullable(treasureHunterItems).orElse(CommonConfigService::defaultTreasureHunterItems);
    }

    public static List<String> treasureHunterItems() {
        return treasureHunterItems.get();
    }

    public static List<String> defaultTreasureHunterItems() {
        return List.of(
                "minecraft:flint",
                "minecraft:clay_ball",
                "trashList[minecraft:feather;minecraft:bone_meal]",
                "lostToolList[minecraft:stick;minecraft:wooden_pickaxe{Damage:59};minecraft:wooden_shovel{Damage:59};minecraft:wooden_axe{Damage:59}]",
                "discList[minecraft:music_disc_13;minecraft:music_disc_cat;minecraft:music_disc_blocks;minecraft:music_disc_chirp;minecraft:music_disc_far;minecraft:music_disc_mall;minecraft:music_disc_mellohi;minecraft:music_disc_stal;minecraft:music_disc_strad;minecraft:music_disc_ward;minecraft:music_disc_11;minecraft:music_disc_wait]",
                "seedList[minecraft:beetroot_seeds;minecraft:wheat_seeds;minecraft:pumpkin_seeds;minecraft:melon_seeds;minecraft:brown_mushroom;minecraft:red_mushroom]",
                "mineralList[minecraft:raw_iron;minecraft:raw_gold;minecraft:raw_copper;minecraft:coal;minecraft:charcoal]");
    }

    public static void setConvergenceItems(Supplier<List<String>> convergenceItems) {
        CommonConfigService.convergenceItems =
                Optional.ofNullable(convergenceItems).orElse(CommonConfigService::defaultConvergenceItems);
    }

    public static List<String> convergenceItems() {
        return convergenceItems.get();
    }

    public static List<String> defaultConvergenceItems() {
        return List.of(
                "minecraft:iron_sword#minecraft:iron_ingot",
                "minecraft:iron_pickaxe#minecraft:iron_ingot",
                "minecraft:iron_axe#minecraft:iron_ingot",
                "minecraft:iron_shovel#minecraft:iron_nugget",
                "minecraft:iron_hoe#minecraft:iron_ingot",
                "minecraft:golden_sword#minecraft:gold_ingot",
                "minecraft:golden_pickaxe#minecraft:gold_ingot",
                "minecraft:golden_axe#minecraft:gold_ingot",
                "minecraft:golden_shovel#minecraft:gold_ingot",
                "minecraft:golden_hoe#minecraft:gold_ingot",
                "minecraft:diamond_sword#minecraft:diamond",
                "minecraft:diamond_pickaxe#minecraft:diamond",
                "minecraft:diamond_axe#minecraft:diamond",
                "minecraft:chainmail_helmet#minecraft:iron_nugget",
                "minecraft:chainmail_chestplate#minecraft:iron_nugget",
                "minecraft:chainmail_leggings#minecraft:iron_nugget",
                "minecraft:chainmail_boots#minecraft:iron_nugget",
                "minecraft:iron_helmet#minecraft:iron_ingot",
                "minecraft:iron_chestplate#minecraft:iron_ingot",
                "minecraft:iron_leggings#minecraft:iron_ingot",
                "minecraft:iron_boots#minecraft:iron_ingot",
                "minecraft:golden_helmet#minecraft:gold_ingot",
                "minecraft:golden_chestplate#minecraft:gold_ingot",
                "minecraft:golden_leggings#minecraft:gold_ingot",
                "minecraft:golden_boots#minecraft:gold_ingot",
                "minecraft:diamond_helmet#minecraft:diamond",
                "minecraft:diamond_chestplate#minecraft:diamond",
                "minecraft:diamond_leggings#minecraft:diamond",
                "minecraft:diamond_boots#minecraft:diamond",
                "minecraft:netherite_upgrade_smithing_template#minecraft:diamond",
                "minecraft:sentry_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:vex_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:wild_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:coast_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:dune_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:wayfinder_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:raiser_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:shaper_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:host_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:ward_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:silence_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:tide_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:snout_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:rib_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:eye_armor_trim_smithing_template#minecraft:diamond",
                "minecraft:spire_armor_trim_smithing_template#minecraft:diamond");
    }
}
