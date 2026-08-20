package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.config.TitleConfigService;
import com.seniors.justlevelingfork.config.conditions.AptitudeLevelProvider;
import com.seniors.justlevelingfork.handler.HandlerConditions;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public final class PlayerProgressService {
    private static Function<ServerPlayer, Optional<PlayerProgress>> progressProvider = player -> Optional.empty();
    private static BiConsumer<ServerPlayer, PlayerProgress> changeListener = (player, progress) -> {};
    private static BiConsumer<ServerPlayer, PlayerProgress> syncHandler = (player, progress) -> {};
    private static final AptitudeLevelProvider APTITUDE_LEVEL_PROVIDER =
            (player, aptitudeName) -> get(player).map(progress -> progress.getAptitudeLevel(aptitudeName)).orElse(1);

    private PlayerProgressService() {
    }

    public static void setProgressProvider(Function<ServerPlayer, Optional<PlayerProgress>> progressProvider) {
        PlayerProgressService.progressProvider = Optional.ofNullable(progressProvider).orElse(player -> Optional.empty());
    }

    public static void setChangeListener(BiConsumer<ServerPlayer, PlayerProgress> changeListener) {
        PlayerProgressService.changeListener = Optional.ofNullable(changeListener).orElse((player, progress) -> {});
    }

    public static void setSyncHandler(BiConsumer<ServerPlayer, PlayerProgress> syncHandler) {
        PlayerProgressService.syncHandler = Optional.ofNullable(syncHandler).orElse((player, progress) -> {});
    }

    public static Optional<PlayerProgress> get(ServerPlayer player) {
        return player == null ? Optional.empty() : progressProvider.apply(player);
    }

    public static AptitudeLevelProvider aptitudeLevelProvider() {
        return APTITUDE_LEVEL_PROVIDER;
    }

    public static int getAbilityScore(ServerPlayer player, Aptitude aptitude) {
        if (aptitude == null) {
            return 10;
        }

        int externalBonus =
                ExternalAbilityBonusService.getBonus(player, aptitude);

        return get(player)
                .map(progress -> AbilityScoreService.abilityScore(
                        progress.getAptitudeLevel(aptitude),
                        externalBonus))
                .orElse(10 + externalBonus);
    }

    public static int getAbilityModifier(ServerPlayer player, Aptitude aptitude) {
        if (aptitude == null) {
            return 0;
        }

        int externalBonus =
                ExternalAbilityBonusService.getBonus(player, aptitude);

        return get(player)
                .map(progress -> AbilityScoreService.abilityModifier(
                        progress.getAptitudeLevel(aptitude),
                        externalBonus))
                .orElse(Math.floorDiv(externalBonus, 2));
    }

    public static int getAbilityScore(
            ServerPlayer player,
            Aptitude aptitude,
            int externalBonus) {

        if (aptitude == null) {
            return 10 + externalBonus;
        }

        return get(player)
                .map(progress -> AbilityScoreService.abilityScore(
                        progress.getAptitudeLevel(aptitude),
                        externalBonus))
                .orElse(10 + externalBonus);
    }

    public static int getAbilityModifier(
            ServerPlayer player,
            Aptitude aptitude,
            int externalBonus) {

        if (aptitude == null) {
            return Math.floorDiv(externalBonus, 2);
        }

        return get(player)
                .map(progress -> AbilityScoreService.abilityModifier(
                        progress.getAptitudeLevel(aptitude),
                        externalBonus))
                .orElse(Math.floorDiv(externalBonus, 2));
    }

    public static boolean update(ServerPlayer player, Consumer<PlayerProgress> mutation) {
        return update(player, mutation, true);
    }

    private static boolean update(ServerPlayer player, Consumer<PlayerProgress> mutation, boolean refreshTitles) {
        if (mutation == null) {
            return false;
        }

        return get(player)
                .map(progress -> {
                    mutation.accept(progress);
                    AbilityDerivedAttributeService.refresh(player, progress);

                    if (refreshTitles) {
                        refreshUnlockedTitles(player, progress);
                    }
                    normalizeSelectedTitle(player, progress);
                    changeListener.accept(player, progress);
                    syncHandler.accept(player, progress);
                    return true;
                })
                .orElse(false);
    }

    public static boolean sync(ServerPlayer player) {
        return get(player)
                .map(progress -> {
                    AbilityDerivedAttributeService.refresh(player, progress);
                    refreshUnlockedTitles(player, progress);
                    normalizeSelectedTitle(player, progress);
                    changeListener.accept(player, progress);
                    syncHandler.accept(player, progress);
                    return true;
                })
                .orElse(false);
    }

    public static boolean addAptitudeLevel(ServerPlayer player, Aptitude aptitude, int amount, int maxLevel) {
        return update(player, progress -> progress.addAptitudeLevel(aptitude, amount, maxLevel));
    }

    public static boolean setAptitudeLevel(ServerPlayer player, Aptitude aptitude, int level, int maxLevel) {
        if (aptitude == null) {
            return false;
        }

        return update(player, progress -> progress.setAptitudeLevel(aptitude, Math.min(Math.max(1, level), maxLevel)));
    }

    public static boolean addPassiveLevel(ServerPlayer player, Passive passive, int amount) {
        return update(player, progress -> {
            progress.addPassiveLevel(passive, amount);
            RegistryAttributes.applyPassiveModifier(player, passive, progress.getPassiveLevel(passive));
        });
    }

    public static boolean subPassiveLevel(ServerPlayer player, Passive passive, int amount) {
        return update(player, progress -> {
            progress.subPassiveLevel(passive, amount);
            RegistryAttributes.applyPassiveModifier(player, passive, progress.getPassiveLevel(passive));
        });
    }

    public static boolean setPlayerTitle(ServerPlayer player, Title title) {
        if (title == null) {
            return false;
        }

        return update(player, progress -> {
            progress.setPlayerTitle(title);
            player.setCustomName(title == RegistryTitles.TITLELESS ? null : Component.translatable(title.getKey()));
        });
    }

    public static boolean setUnlockTitle(ServerPlayer player, Title title, boolean unlocked) {
        if (title == null) {
            return false;
        }

        return update(player, progress -> {
            boolean wasUnlocked = progress.getLockTitle(title);
            progress.setUnlockTitle(title, unlocked);
            normalizeSelectedTitle(player, progress);
            if (unlocked && !wasUnlocked) {
                TitleUnlockService.send(player, title.getName());
            }
        }, false);
    }

    public static boolean setToggleSkill(ServerPlayer player, String skillName, boolean enabled) {
        if (skillName == null || skillName.isBlank()) {
            return false;
        }

        return update(player, progress -> progress.setToggleSkill(skillName, enabled));
    }

    public static boolean toggleSkill(ServerPlayer player, String skillName) {
        if (skillName == null || skillName.isBlank()) {
            return false;
        }

        return update(player, progress -> progress.setToggleSkill(skillName, !progress.getToggleSkill(skillName)));
    }

    public static boolean isSkillEnabled(ServerPlayer player, Skill skill) {
        if (skill == null) {
            return false;
        }

        return get(player)
                .map(progress -> skill.isEnabled(
                        progress.getAptitudeLevel(skill.aptitude), progress.getToggleSkill(skill)))
                .orElse(false);
    }

    public static boolean armCounterAttack(ServerPlayer player, float damage) {
        if (damage <= 0.0F || !isSkillEnabled(player, com.seniors.justlevelingfork.registry.RegistrySkills.COUNTER_ATTACK)) {
            return false;
        }

        return update(player, progress -> {
            progress.counterAttack = true;
            progress.counterAttackTimer = 0;
            progress.counterAttackDamage = damage;
        });
    }

    public static void tickCounterAttack(ServerPlayer player) {
        get(player).ifPresent(progress -> {
            if (!progress.counterAttack) {
                return;
            }

            progress.counterAttackTimer++;
            if (progress.counterAttackTimer >= com.seniors.justlevelingfork.registry.RegistrySkills.COUNTER_ATTACK.getValue()[0] * 40.0D) {
                clearCounterAttack(player, progress);
            }
        });
    }

    public static float consumeCounterAttackDamage(ServerPlayer player) {
        return get(player)
                .map(progress -> {
                    if (!progress.counterAttack) {
                        return 0.0F;
                    }

                    float damage = progress.counterAttackDamage;
                    clearCounterAttack(player, progress);
                    return damage;
                })
                .orElse(0.0F);
    }

    private static void clearCounterAttack(ServerPlayer player, PlayerProgress progress) {
        progress.counterAttack = false;
        progress.counterAttackTimer = 0;
        progress.counterAttackDamage = 0.0F;
        changeListener.accept(player, progress);
        syncHandler.accept(player, progress);
    }

    public static boolean refreshPassiveModifiers(ServerPlayer player) {
        return get(player)
                .map(progress -> {
                    RegistryPassives.values().forEach(passive ->
                            RegistryAttributes.applyPassiveModifier(player, passive, progress.getPassiveLevel(passive)));
                    return true;
                })
                .orElse(false);
    }

    public static boolean resetSkills(ServerPlayer player) {
        return get(player)
                .map(progress -> {
                    int refundedExperience = CommonConfigService.skillResetRefundsSpentLevels()
                            ? progress.getSpentAptitudeExperience(level -> AptitudeExperience.requiredPoints(
                                    level, CommonConfigService.aptitudeFirstCostLevel()))
                            : 0;
                    progress.resetSkills();
                    if (refundedExperience > 0 && !player.getAbilities().instabuild) {
                        AptitudeExperience.addPlayerXP(player, refundedExperience);
                    }
                    RegistryPassives.values().forEach(passive ->
                            RegistryAttributes.applyPassiveModifier(player, passive, progress.getPassiveLevel(passive)));
                    refreshUnlockedTitles(player, progress);
                    normalizeSelectedTitle(player, progress);
                    changeListener.accept(player, progress);
                    syncHandler.accept(player, progress);
                    return true;
                })
                .orElse(false);
    }

    private static void refreshUnlockedTitles(ServerPlayer player, PlayerProgress progress) {
        TitleConfigService.titleModels().forEach(titleModel -> {
            Title title = RegistryTitles.getTitle(titleModel.TitleId);
            if (title == null || progress.getLockTitle(title)) {
                return;
            }

            if (titleModel.checkRequirements(player, HandlerConditions::getConditionByName)) {
                progress.setUnlockTitle(title, true);
                TitleUnlockService.send(player, title.getName());
            }
        });

        Title admin = RegistryTitles.ADMIN;
        if (!progress.getLockTitle(admin) && player.hasPermissions(2)) {
            progress.setUnlockTitle(admin, true);
            TitleUnlockService.send(player, admin.getName());
        }
    }

    private static void normalizeSelectedTitle(ServerPlayer player, PlayerProgress progress) {
        Title selectedTitle = RegistryTitles.getTitle(progress.playerTitle);
        if (selectedTitle == null || !progress.getLockTitle(selectedTitle)) {
            selectedTitle = RegistryTitles.TITLELESS;
            progress.setPlayerTitle(selectedTitle);
            player.setCustomName(null);
        }
    }
}
