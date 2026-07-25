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
                    int requiredPoints = AptitudeExperience.requiredPoints(
                            aptitudeLevel, CommonConfigService.aptitudeFirstCostLevel());
                    int requiredExperienceLevels = AptitudeExperience.requiredExperienceLevels(
                            aptitudeLevel, CommonConfigService.aptitudeFirstCostLevel());
                    boolean canLevelUp = player.isCreative()
                            || requiredPoints <= player.totalExperience
                            || requiredExperienceLevels <= player.experienceLevel;

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
                            player, aptitude, 1, CommonConfigService.aptitudeMaxLevel());
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
        return PlayerProgressService.addPassiveLevel(player, passive, 1);
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
        return PlayerProgressService.setPlayerTitle(player, title);
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
        if (skillName == null || skillName.isBlank()) {
            Constants.LOG.warn("Received skill toggle request with no skill name, skipping request...");
            return false;
        }
        return PlayerProgressService.setToggleSkill(player, skillName, enabled);
    }

    public static boolean toggleSkill(ServerPlayer player, String skillName) {
        if (skillName == null || skillName.isBlank()) {
            Constants.LOG.warn("Received skill toggle request with no skill name, skipping request...");
            return false;
        }
        return PlayerProgressService.toggleSkill(player, skillName);
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
}
