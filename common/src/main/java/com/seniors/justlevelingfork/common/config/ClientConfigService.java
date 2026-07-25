package com.seniors.justlevelingfork.common.config;

import com.seniors.justlevelingfork.client.core.SortPassives;
import com.seniors.justlevelingfork.client.core.SortSkills;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class ClientConfigService {
    private static BooleanSupplier showLuckyDropSkillOverlay = () -> true;
    private static BooleanSupplier showCriticalRollSkillOverlay = () -> true;
    private static BooleanSupplier showSkillModName = () -> false;
    private static BooleanSupplier showTitleModName = () -> false;
    private static Supplier<SortPassives> passiveSort = () -> SortPassives.ByName;
    private static Supplier<SortSkills> skillSort = () -> SortSkills.ByLevel;

    private ClientConfigService() {
    }

    public static void setShowLuckyDropSkillOverlay(BooleanSupplier showLuckyDropSkillOverlay) {
        ClientConfigService.showLuckyDropSkillOverlay =
                Optional.ofNullable(showLuckyDropSkillOverlay).orElse(() -> true);
    }

    public static void setShowCriticalRollSkillOverlay(BooleanSupplier showCriticalRollSkillOverlay) {
        ClientConfigService.showCriticalRollSkillOverlay =
                Optional.ofNullable(showCriticalRollSkillOverlay).orElse(() -> true);
    }

    public static void setShowSkillModName(BooleanSupplier showSkillModName) {
        ClientConfigService.showSkillModName = Optional.ofNullable(showSkillModName).orElse(() -> false);
    }

    public static boolean showSkillModName() {
        return showSkillModName.getAsBoolean();
    }

    public static void setShowTitleModName(BooleanSupplier showTitleModName) {
        ClientConfigService.showTitleModName = Optional.ofNullable(showTitleModName).orElse(() -> false);
    }

    public static boolean showTitleModName() {
        return showTitleModName.getAsBoolean();
    }

    public static void setPassiveSort(Supplier<SortPassives> passiveSort) {
        ClientConfigService.passiveSort = Optional.ofNullable(passiveSort).orElse(() -> SortPassives.ByName);
    }

    public static SortPassives passiveSort() {
        return Optional.ofNullable(passiveSort.get()).orElse(SortPassives.ByName);
    }

    public static void setSkillSort(Supplier<SortSkills> skillSort) {
        ClientConfigService.skillSort = Optional.ofNullable(skillSort).orElse(() -> SortSkills.ByLevel);
    }

    public static SortSkills skillSort() {
        return Optional.ofNullable(skillSort.get()).orElse(SortSkills.ByLevel);
    }

    public static boolean shouldShowSkillMessage(String translationKey) {
        if ("overlay.skill.justlevelingfork.lucky_drop".equals(translationKey)) {
            return showLuckyDropSkillOverlay.getAsBoolean();
        }
        if ("overlay.skill.justlevelingfork.critical_roll_1".equals(translationKey)
                || "overlay.skill.justlevelingfork.critical_roll_6".equals(translationKey)) {
            return showCriticalRollSkillOverlay.getAsBoolean();
        }
        return true;
    }
}
