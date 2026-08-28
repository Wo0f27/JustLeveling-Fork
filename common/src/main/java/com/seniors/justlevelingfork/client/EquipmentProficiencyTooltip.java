package com.seniors.justlevelingfork.client;

import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import com.seniors.justlevelingfork.common.proficiency.ArmorClassificationService;
import com.seniors.justlevelingfork.common.proficiency.ProficiencyClientState;
import com.seniors.justlevelingfork.common.proficiency.WeaponClassificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class EquipmentProficiencyTooltip {

    private static final String WEAPON_TYPE_PREFIX =
            "weapons/types/";

    private EquipmentProficiencyTooltip() {
    }

    public static void append(
            ItemStack stack,
            List<Component> tooltip) {

        if (stack == null
                || stack.isEmpty()
                || tooltip == null) {

            return;
        }

        Set<ResourceLocation> weaponClassifications =
                WeaponClassificationService
                        .getClassifications(stack);

        if (!weaponClassifications.isEmpty()) {

            appendWeaponTooltip(
                    stack,
                    weaponClassifications,
                    tooltip);

            return;
        }

        Set<ArmorCategory> armorCategories =
                ArmorClassificationService
                        .getCategories(stack);

        if (!armorCategories.isEmpty()) {

            appendArmorTooltip(
                    armorCategories,
                    tooltip);
        }
    }

    private static void appendWeaponTooltip(
            ItemStack stack,
            Set<ResourceLocation> classifications,
            List<Component> tooltip) {

        List<ResourceLocation> displayTypes =
                new ArrayList<>();

        /*
         * Prefer specific D&D-style weapon types.
         *
         * For example:
         *
         * Martial + Longsword + Shortsword
         *
         * displays:
         *
         * Longsword, Shortsword
         */
        for (ResourceLocation classification
                : classifications) {

            if (classification != null
                    && classification
                    .getPath()
                    .startsWith(
                            WEAPON_TYPE_PREFIX)) {

                displayTypes.add(
                        classification);
            }
        }

        /*
         * Fallback for a datapack item which only has a broad
         * Simple/Martial classification.
         */
        if (displayTypes.isEmpty()) {

            displayTypes.addAll(
                    classifications);
        }

        StringJoiner typeNames =
                new StringJoiner(", ");

        for (ResourceLocation type
                : displayTypes) {

            typeNames.add(
                    displayName(type));
        }

        appendClassificationLine(
                tooltip,
                "tooltip.justlevelingfork.weapon_type",
                typeNames.toString());

        if (!ProficiencyClientState
                .isSynchronized()) {

            return;
        }

        boolean proficient =
                isWeaponProficient(stack);

        appendProficiencyLine(
                tooltip,
                proficient);
    }

    private static void appendArmorTooltip(
            Set<ArmorCategory> categories,
            List<Component> tooltip) {

        StringJoiner typeNames =
                new StringJoiner(", ");

        for (ArmorCategory category
                : categories) {

            if (category != null) {

                typeNames.add(
                        displayName(
                                category.name()));
            }
        }

        appendClassificationLine(
                tooltip,
                "tooltip.justlevelingfork.armor_type",
                typeNames.toString());

        if (!ProficiencyClientState
                .isSynchronized()) {

            return;
        }

        /*
         * Mirrors ArmorProficiencyService:
         *
         * armor uses ALL-match semantics.
         */
        boolean proficient =
                ProficiencyClientState
                        .armorProficiencies()
                        .containsAll(categories);

        appendProficiencyLine(
                tooltip,
                proficient);
    }

    private static boolean isWeaponProficient(
            ItemStack stack) {

        /*
         * Mirrors WeaponProficiencyService:
         *
         * weapon proficiency uses ANY-match semantics.
         *
         * The client is NOT determining where these
         * proficiencies came from. It only evaluates the
         * authoritative server-synchronized proficiency set
         * against the item's tags.
         */
        for (ResourceLocation proficiencyId
                : ProficiencyClientState
                .weaponProficiencies()) {

            if (WeaponClassificationService.matches(
                    stack,
                    proficiencyId)) {

                return true;
            }
        }

        return false;
    }

    private static void appendClassificationLine(
            List<Component> tooltip,
            String labelKey,
            String value) {

        MutableComponent line =
                Component.translatable(
                                labelKey)
                        .withStyle(
                                ChatFormatting.GRAY);

        line.append(
                Component.literal(
                                " " + value)
                        .withStyle(
                                ChatFormatting.WHITE));

        tooltip.add(line);
    }

    private static void appendProficiencyLine(
            List<Component> tooltip,
            boolean proficient) {

        MutableComponent line =
                Component.translatable(
                                "tooltip.justlevelingfork.proficiency")
                        .withStyle(
                                ChatFormatting.GRAY);

        line.append(
                Component.literal(" "));

        line.append(
                Component.translatable(
                                proficient
                                        ? "tooltip.justlevelingfork.proficient"
                                        : "tooltip.justlevelingfork.not_proficient")
                        .withStyle(
                                proficient
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED));

        tooltip.add(line);
    }

    private static String displayName(
            ResourceLocation id) {

        if (id == null) {
            return "";
        }

        String path =
                id.getPath();

        int slash =
                path.lastIndexOf('/');

        if (slash >= 0
                && slash + 1
                < path.length()) {

            path =
                    path.substring(
                            slash + 1);
        }

        return displayName(path);
    }

    private static String displayName(
            String rawName) {

        if (rawName == null
                || rawName.isBlank()) {

            return "";
        }

        String[] words =
                rawName
                        .toLowerCase(Locale.ROOT)
                        .split("_");

        StringJoiner result =
                new StringJoiner(" ");

        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            result.add(
                    Character
                            .toUpperCase(
                                    word.charAt(0))
                            + word.substring(1));
        }

        return result.toString();
    }
}