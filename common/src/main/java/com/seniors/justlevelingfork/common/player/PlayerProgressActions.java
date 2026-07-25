package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.integration.AptitudeLevelUpEvents;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

public final class PlayerProgressActions {
    private PlayerProgressActions() {
    }

    public static boolean levelUpAptitude(ServerPlayer player, Aptitude aptitude) {
        if (player == null || aptitude == null) {
            return false;
        }

        return PlayerProgressService.get(player)
                .map(progress -> {
                    int aptitudeLevel = progress.getAptitudeLevel(aptitude);
                    int aptitudeMaxLevel = CommonConfigService.aptitudeMaxLevel();
                    if (aptitudeLevel >= aptitudeMaxLevel) {
                        Constants.LOG.info(
                                "Received level up request at or above the configured aptitude level limit, skipping request...");
                        return false;
                    }

                    int requiredPoints = AptitudeExperience.requiredPoints(
                            aptitudeLevel, CommonConfigService.aptitudeFirstCostLevel());
                    boolean canLevelUp = player.isCreative()
                            || requiredPoints <= AptitudeExperience.getPlayerXP(player);

                    if (!canLevelUp) {
                        Constants.LOG.info(
                                "Received level up request without the required EXP needed to level up, skipping request...");
                        return false;
                    }
                    if (progress.getGlobalLevel() >= CommonConfigService.playersMaxGlobalLevel()) {
                        Constants.LOG.info(
                                "Received level up request over the configured global level limit, skipping request...");
                        return false;
                    }
                    if (AptitudeLevelUpEvents.isCancelled(player, aptitude)) {
                        return false;
                    }

                    boolean changed = PlayerProgressService.addAptitudeLevel(
                            player, aptitude, 1, aptitudeMaxLevel);
                    if (changed && !player.isCreative()) {
                        AptitudeExperience.addPlayerXP(player, -requiredPoints);
                    }
                    return changed;
                })
                .orElse(false);
    }

    public static boolean levelUpAptitude(ServerPlayer player, String aptitudeName) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName);
        if (aptitude == null) {
            Constants.LOG.warn("Received aptitude level-up request for unknown aptitude '{}', skipping request...", aptitudeName);
            return false;
        }
        return levelUpAptitude(player, aptitude);
    }

    public static boolean levelUpPassive(ServerPlayer player, Passive passive) {
        if (player == null || passive == null || RegistryPassives.getPassive(passive.getName()) != passive) {
            return false;
        }

        return PlayerProgressService.get(player)
                .map(progress -> {
                    int passiveLevel = progress.getPassiveLevel(passive);
                    if (passiveLevel >= passive.getMaxLevel()
                            || progress.getAptitudeLevel(passive.aptitude) < passive.getNextLevelUp(passiveLevel)) {
                        return false;
                    }

                    return PlayerProgressService.addPassiveLevel(player, passive, 1);
                })
                .orElse(false);
    }

    public static boolean levelUpPassive(ServerPlayer player, String passiveName) {
        Passive passive = RegistryPassives.getPassive(passiveName);
        if (passive == null) {
            Constants.LOG.warn("Received passive level-up request for unknown passive '{}', skipping request...", passiveName);
            return false;
        }
        return levelUpPassive(player, passive);
    }

    public static boolean levelDownPassive(ServerPlayer player, Passive passive) {
        return PlayerProgressService.subPassiveLevel(player, passive, 1);
    }

    public static boolean levelDownPassive(ServerPlayer player, String passiveName) {
        Passive passive = RegistryPassives.getPassive(passiveName);
        if (passive == null) {
            Constants.LOG.warn("Received passive level-down request for unknown passive '{}', skipping request...", passiveName);
            return false;
        }
        return levelDownPassive(player, passive);
    }

    public static boolean setPlayerTitle(ServerPlayer player, Title title) {
        if (player == null || title == null) {
            return false;
        }

        return PlayerProgressService.get(player)
                .filter(progress -> progress.getLockTitle(title))
                .map(progress -> PlayerProgressService.setPlayerTitle(player, title))
                .orElse(false);
    }

    public static boolean setPlayerTitle(ServerPlayer player, String titleName) {
        Title title = RegistryTitles.getTitle(titleName);
        if (title == null) {
            Constants.LOG.warn("Received title selection request for unknown title '{}', skipping request...", titleName);
            return false;
        }
        return setPlayerTitle(player, title);
    }

    public static boolean setToggleSkill(ServerPlayer player, String skillName, boolean enabled) {
        Skill skill = eligibleSkill(player, skillName);
        if (skill == null) {
            return false;
        }
        return PlayerProgressService.setToggleSkill(player, skill.getName(), enabled);
    }

    public static boolean toggleSkill(ServerPlayer player, String skillName) {
        Skill skill = eligibleSkill(player, skillName);
        if (skill == null) {
            return false;
        }
        return PlayerProgressService.toggleSkill(player, skill.getName());
    }

    public static boolean openEnderChest(ServerPlayer player) {
        if (player == null || !PlayerProgressService.isSkillEnabled(player, RegistrySkills.WORMHOLE_STORAGE)) {
            return false;
        }

        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> ChestMenu.threeRows(id, inventory, enderChest),
                Component.translatable(RegistrySkills.WORMHOLE_STORAGE.getKey())));
        return true;
    }

    private static Skill eligibleSkill(ServerPlayer player, String skillName) {
        if (player == null || skillName == null || skillName.isBlank()) {
            Constants.LOG.warn("Received skill toggle request with no skill name, skipping request...");
            return null;
        }

        Skill skill = RegistrySkills.getSkill(skillName);
        if (skill == null) {
            Constants.LOG.warn("Received skill toggle request for unknown skill '{}', skipping request...", skillName);
            return null;
        }

        boolean canToggle = PlayerProgressService.get(player)
                .map(progress -> skill.canToggleAtLevel(progress.getAptitudeLevel(skill.aptitude)))
                .orElse(false);
        return canToggle ? skill : null;
    }
}
