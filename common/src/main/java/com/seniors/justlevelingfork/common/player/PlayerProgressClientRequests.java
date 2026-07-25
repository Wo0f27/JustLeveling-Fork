package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PlayerProgressClientRequests {
    private static Consumer<String> aptitudeLevelUpSender = ignored -> {};
    private static Consumer<String> passiveLevelUpSender = ignored -> {};
    private static Consumer<String> passiveLevelDownSender = ignored -> {};
    private static Consumer<String> setPlayerTitleSender = ignored -> {};
    private static BiConsumer<String, Boolean> setToggleSkillSender = (ignored, enabled) -> {};
    private static Runnable openEnderChestSender = () -> {};

    private PlayerProgressClientRequests() {
    }

    public static void setAptitudeLevelUpSender(Consumer<String> sender) {
        aptitudeLevelUpSender = Optional.ofNullable(sender).orElse(ignored -> {});
    }

    public static void setPassiveLevelUpSender(Consumer<String> sender) {
        passiveLevelUpSender = Optional.ofNullable(sender).orElse(ignored -> {});
    }

    public static void setPassiveLevelDownSender(Consumer<String> sender) {
        passiveLevelDownSender = Optional.ofNullable(sender).orElse(ignored -> {});
    }

    public static void setPlayerTitleSender(Consumer<String> sender) {
        setPlayerTitleSender = Optional.ofNullable(sender).orElse(ignored -> {});
    }

    public static void setToggleSkillSender(BiConsumer<String, Boolean> sender) {
        setToggleSkillSender = Optional.ofNullable(sender).orElse((ignored, enabled) -> {});
    }

    public static void setOpenEnderChestSender(Runnable sender) {
        openEnderChestSender = Optional.ofNullable(sender).orElse(() -> {});
    }

    public static void requestAptitudeLevelUp(Aptitude aptitude) {
        if (aptitude != null) {
            requestAptitudeLevelUp(aptitude.getName());
        }
    }

    public static void requestAptitudeLevelUp(String aptitudeName) {
        if (hasText(aptitudeName)) {
            aptitudeLevelUpSender.accept(aptitudeName);
        }
    }

    public static void requestPassiveLevelUp(Passive passive) {
        if (passive != null) {
            requestPassiveLevelUp(passive.getName());
        }
    }

    public static void requestPassiveLevelUp(String passiveName) {
        if (hasText(passiveName)) {
            passiveLevelUpSender.accept(passiveName);
        }
    }

    public static void requestPassiveLevelDown(Passive passive) {
        if (passive != null) {
            requestPassiveLevelDown(passive.getName());
        }
    }

    public static void requestPassiveLevelDown(String passiveName) {
        if (hasText(passiveName)) {
            passiveLevelDownSender.accept(passiveName);
        }
    }

    public static void requestSetPlayerTitle(Title title) {
        if (title != null) {
            requestSetPlayerTitle(title.getName());
        }
    }

    public static void requestSetPlayerTitle(String titleName) {
        if (hasText(titleName)) {
            setPlayerTitleSender.accept(titleName);
        }
    }

    public static void requestSetToggleSkill(Skill skill, boolean enabled) {
        if (skill != null) {
            requestSetToggleSkill(skill.getName(), enabled);
        }
    }

    public static void requestSetToggleSkill(String skillName, boolean enabled) {
        if (hasText(skillName)) {
            setToggleSkillSender.accept(skillName, enabled);
        }
    }

    public static void requestOpenEnderChest() {
        openEnderChestSender.run();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
