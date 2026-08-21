package com.seniors.justlevelingfork.common.proficiency;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WeaponUsageService {

    private WeaponUsageService() {
    }

    public static ItemStack getMainHandWeapon(
            ServerPlayer player) {

        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack =
                player.getMainHandItem();

        if (!WeaponClassificationService
                .isClassifiedWeapon(stack)) {
            return ItemStack.EMPTY;
        }

        return stack;
    }

    public static ItemStack getOffhandWeapon(
            ServerPlayer player) {

        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack =
                player.getOffhandItem();

        if (!WeaponClassificationService
                .isClassifiedWeapon(stack)) {
            return ItemStack.EMPTY;
        }

        return stack;
    }

    public static boolean isHoldingClassifiedWeapon(
            ServerPlayer player) {

        return !getMainHandWeapon(player)
                .isEmpty();
    }

    public static boolean isHoldingProficientWeapon(
            ServerPlayer player) {

        ItemStack stack =
                getMainHandWeapon(player);

        if (stack.isEmpty()) {
            return false;
        }

        return WeaponProficiencyService
                .isProficient(
                        player,
                        stack);
    }

    public static boolean isHoldingNonProficientWeapon(
            ServerPlayer player) {

        ItemStack stack =
                getMainHandWeapon(player);

        if (stack.isEmpty()) {
            return false;
        }

        return !WeaponProficiencyService
                .isProficient(
                        player,
                        stack);
    }

    public static boolean isOffhandWeaponProficient(
            ServerPlayer player) {

        ItemStack stack =
                getOffhandWeapon(player);

        if (stack.isEmpty()) {
            return false;
        }

        return WeaponProficiencyService
                .isProficient(
                        player,
                        stack);
    }

    public static boolean isOffhandWeaponNonProficient(
            ServerPlayer player) {

        ItemStack stack =
                getOffhandWeapon(player);

        if (stack.isEmpty()) {
            return false;
        }

        return !WeaponProficiencyService
                .isProficient(
                        player,
                        stack);
    }

    public static boolean isHoldingWeaponType(
            ServerPlayer player,
            ResourceLocation proficiencyId) {

        if (player == null
                || proficiencyId == null) {
            return false;
        }

        ItemStack stack =
                getMainHandWeapon(player);

        return !stack.isEmpty()
                && WeaponClassificationService
                .matches(
                        stack,
                        proficiencyId);
    }

    public static Set<ResourceLocation>
    getMainHandClassifications(
            ServerPlayer player) {

        ItemStack stack =
                getMainHandWeapon(player);

        if (stack.isEmpty()) {
            return Set.of();
        }

        return WeaponClassificationService
                .getClassifications(stack);
    }

    public static Set<ResourceLocation>
    getMatchingMainHandProficiencies(
            ServerPlayer player) {

        ItemStack stack =
                getMainHandWeapon(player);

        if (stack.isEmpty()) {
            return Set.of();
        }

        return WeaponProficiencyService
                .getMatchingProficiencies(
                        player,
                        stack);
    }

    public static boolean isHoldingSimpleWeapon(
            ServerPlayer player) {

        return isHoldingWeaponType(
                player,
                com.seniors.justlevelingfork.registry
                        .RegistryWeaponProficiencies.SIMPLE);
    }

    public static boolean isHoldingMartialWeapon(
            ServerPlayer player) {

        return isHoldingWeaponType(
                player,
                com.seniors.justlevelingfork.registry
                        .RegistryWeaponProficiencies.MARTIAL);
    }
}