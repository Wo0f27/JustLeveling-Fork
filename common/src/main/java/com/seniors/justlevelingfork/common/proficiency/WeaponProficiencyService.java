package com.seniors.justlevelingfork.common.proficiency;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.player.CharacterClassDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WeaponProficiencyService {

    @FunctionalInterface
    public interface Provider {

        Set<ResourceLocation> getProficiencies(
                ServerPlayer player);
    }

    private static final Map<ResourceLocation, Provider> PROVIDERS =
            new ConcurrentHashMap<>();

    private WeaponProficiencyService() {
    }

    public static boolean registerProvider(
            ResourceLocation id,
            Provider provider) {

        if (id == null || provider == null) {
            return false;
        }

        PROVIDERS.put(
                id,
                provider);

        return true;
    }

    public static void unregisterProvider(
            ResourceLocation id) {

        if (id != null) {
            PROVIDERS.remove(id);
        }
    }

    /**
     * Returns all effective weapon proficiency IDs.
     *
     * IDs correspond directly to item tags.
     *
     * Examples:
     *
     * justlevelingfork:weapons/simple
     * justlevelingfork:weapons/types/shortsword
     * anothermod:weapons/types/katana
     */
    public static Set<ResourceLocation> getProficiencies(
            ServerPlayer player) {

        if (player == null) {
            return Set.of();
        }

        Set<ResourceLocation> result =
                new LinkedHashSet<>();

        PlayerProgress progress =
                PlayerProgressService.get(player)
                        .orElse(null);

        if (progress != null) {
            addClassProficiencies(
                    progress,
                    result);
        }

        addProviderProficiencies(
                player,
                result);

        return immutable(result);
    }

    public static Set<ResourceLocation> getClassProficiencies(
            ServerPlayer player) {

        if (player == null) {
            return Set.of();
        }

        PlayerProgress progress =
                PlayerProgressService.get(player)
                        .orElse(null);

        if (progress == null) {
            return Set.of();
        }

        Set<ResourceLocation> result =
                new LinkedHashSet<>();

        addClassProficiencies(
                progress,
                result);

        return immutable(result);
    }

    public static boolean hasProficiency(
            ServerPlayer player,
            ResourceLocation proficiencyId) {

        if (player == null
                || proficiencyId == null) {
            return false;
        }

        return getProficiencies(player)
                .contains(proficiencyId);
    }

    /**
     * A weapon is proficient when at least one proficiency
     * the player owns matches a tag on the item.
     *
     * ANY-match is intentional.
     *
     * A vanilla sword may represent both a Shortsword and
     * Longsword. A Monk's Shortsword proficiency is enough.
     */
    public static boolean isProficient(
            ServerPlayer player,
            ItemStack stack) {

        if (player == null
                || stack == null
                || stack.isEmpty()) {
            return false;
        }

        for (ResourceLocation proficiencyId :
                getProficiencies(player)) {

            if (WeaponClassificationService.matches(
                    stack,
                    proficiencyId)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Returns the player's proficiency IDs which actually
     * match this particular weapon.
     *
     * This also supports arbitrary proficiency IDs registered
     * by external mods.
     */
    public static Set<ResourceLocation> getMatchingProficiencies(
            ServerPlayer player,
            ItemStack stack) {

        if (player == null
                || stack == null
                || stack.isEmpty()) {
            return Set.of();
        }

        Set<ResourceLocation> result =
                new LinkedHashSet<>();

        for (ResourceLocation proficiencyId :
                getProficiencies(player)) {

            if (WeaponClassificationService.matches(
                    stack,
                    proficiencyId)) {

                result.add(proficiencyId);
            }
        }

        return immutable(result);
    }

    private static void addClassProficiencies(
            PlayerProgress progress,
            Set<ResourceLocation> result) {

        ResourceLocation startingClass =
                ResourceLocation.tryParse(
                        progress.getStartingClass());

        for (Map.Entry<String, Integer> entry :
                progress.classLevels.entrySet()) {

            if (entry.getValue() == null
                    || entry.getValue() <= 0) {
                continue;
            }

            ResourceLocation classId =
                    ResourceLocation.tryParse(
                            entry.getKey());

            if (classId == null) {
                continue;
            }

            CharacterClassDefinition definition =
                    RegistryClasses.getDefinition(
                            classId);

            if (definition == null) {
                continue;
            }

            if (classId.equals(startingClass)) {

                result.addAll(
                        definition
                                .startingWeaponProficiencies());

            } else {

                result.addAll(
                        definition
                                .multiclassWeaponProficiencies());
            }
        }
    }

    private static void addProviderProficiencies(
            ServerPlayer player,
            Set<ResourceLocation> result) {

        for (Map.Entry<ResourceLocation, Provider> entry :
                PROVIDERS.entrySet()) {

            try {

                Set<ResourceLocation> provided =
                        entry.getValue()
                                .getProficiencies(player);

                if (provided == null) {
                    continue;
                }

                for (ResourceLocation proficiencyId :
                        provided) {

                    if (proficiencyId != null) {
                        result.add(proficiencyId);
                    }
                }

            } catch (RuntimeException exception) {

                Constants.LOG.warn(
                        "Weapon proficiency provider {} failed.",
                        entry.getKey(),
                        exception);
            }
        }
    }

    private static Set<ResourceLocation> immutable(
            Set<ResourceLocation> values) {

        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                new LinkedHashSet<>(values));
    }
}