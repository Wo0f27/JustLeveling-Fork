package com.seniors.justlevelingfork.common.proficiency;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.player.CharacterClassDefinition;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ArmorProficiencyService {

    /**
     * Generic proficiency provider.
     *
     * Providers may come from JLF itself or another mod.
     *
     * Examples:
     * - ancestry
     * - subclass
     * - feat
     * - temporary feature
     */
    @FunctionalInterface
    public interface Provider {

        Set<ArmorCategory> getProficiencies(
                ServerPlayer player);
    }

    private static final Map<ResourceLocation, Provider> PROVIDERS =
            new ConcurrentHashMap<>();

    private ArmorProficiencyService() {
    }

    /**
     * Registers or replaces a proficiency provider.
     *
     * The ID should be stable and namespaced to the mod
     * registering the provider.
     */
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
     * Returns every effective armor proficiency the player
     * currently has.
     *
     * This combines:
     *
     * 1. Starting-class armor proficiencies
     * 2. Multiclass armor proficiencies
     * 3. Registered proficiency providers
     */
    public static Set<ArmorCategory> getProficiencies(
            ServerPlayer player) {

        if (player == null) {
            return Set.of();
        }

        EnumSet<ArmorCategory> result =
                EnumSet.noneOf(
                        ArmorCategory.class);

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

        if (result.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                EnumSet.copyOf(result));
    }

    /**
     * Returns only proficiency granted through class choice.
     *
     * This excludes ancestries, feats, subclasses and other
     * providers.
     */
    public static Set<ArmorCategory> getClassProficiencies(
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

        EnumSet<ArmorCategory> result =
                EnumSet.noneOf(
                        ArmorCategory.class);

        addClassProficiencies(
                progress,
                result);

        if (result.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(
                EnumSet.copyOf(result));
    }

    public static boolean hasProficiency(
            ServerPlayer player,
            ArmorCategory category) {

        if (player == null || category == null) {
            return false;
        }

        return getProficiencies(player)
                .contains(category);
    }

    /**
     * Checks whether the player is proficient with every
     * category assigned to the supplied item.
     *
     * Normally an item has exactly one category.
     *
     * Requiring all categories is intentionally conservative:
     * if a datapack accidentally classifies an item as both
     * LIGHT and HEAVY, having only Light proficiency will not
     * silently make the malformed item proficient.
     *
     * Unclassified items return false because JLF has no
     * proficiency category to evaluate.
     */
    public static boolean isProficient(
            ServerPlayer player,
            ItemStack stack) {

        if (player == null
                || stack == null
                || stack.isEmpty()) {
            return false;
        }

        Set<ArmorCategory> categories =
                ArmorClassificationService
                        .getCategories(stack);

        if (categories.isEmpty()) {
            return false;
        }

        return getProficiencies(player)
                .containsAll(categories);
    }

    private static void addClassProficiencies(
            PlayerProgress progress,
            EnumSet<ArmorCategory> result) {

        if (progress == null || result == null) {
            return;
        }

        ResourceLocation startingClass =
                ResourceLocation.tryParse(
                        progress.getStartingClass());

        for (Map.Entry<String, Integer> entry
                : progress.classLevels.entrySet()) {

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

            /*
             * Unknown/addon classes simply contribute no
             * built-in JLF proficiency.
             *
             * They can still grant proficiency through a
             * registered provider.
             */
            if (definition == null) {
                continue;
            }

            if (classId.equals(startingClass)) {

                result.addAll(
                        definition
                                .startingArmorProficiencies());

            } else {

                result.addAll(
                        definition
                                .multiclassArmorProficiencies());
            }
        }
    }

    private static void addProviderProficiencies(
            ServerPlayer player,
            EnumSet<ArmorCategory> result) {

        for (Map.Entry<ResourceLocation, Provider> entry
                : PROVIDERS.entrySet()) {

            try {

                Set<ArmorCategory> provided =
                        entry.getValue()
                                .getProficiencies(player);

                if (provided != null) {

                    for (ArmorCategory category
                            : provided) {

                        if (category != null) {
                            result.add(category);
                        }
                    }
                }

            } catch (RuntimeException exception) {

                /*
                 * A broken integration should not break the
                 * player's entire proficiency calculation.
                 */
                Constants.LOG.warn(
                        "Armor proficiency provider {} failed.",
                        entry.getKey(),
                        exception);
            }
        }
    }
}