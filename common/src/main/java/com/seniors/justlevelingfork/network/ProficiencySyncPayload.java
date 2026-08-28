package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.proficiency.ArmorCategory;
import com.seniors.justlevelingfork.common.proficiency.ArmorProficiencyService;
import com.seniors.justlevelingfork.common.proficiency.ProficiencyClientState;
import com.seniors.justlevelingfork.common.proficiency.WeaponProficiencyService;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ProficiencySyncPayload(
        Set<ResourceLocation> weaponProficiencies,
        Set<ArmorCategory> armorProficiencies) {

    private static final int MAX_WEAPON_PROFICIENCIES =
            512;

    private static final int MAX_ARMOR_PROFICIENCIES =
            ArmorCategory.values().length;

    public ProficiencySyncPayload {

        LinkedHashSet<ResourceLocation> weapons =
                new LinkedHashSet<>();

        if (weaponProficiencies != null) {

            for (ResourceLocation id
                    : weaponProficiencies) {

                if (id != null) {
                    weapons.add(id);
                }
            }
        }

        weaponProficiencies =
                weapons.isEmpty()
                        ? Set.of()
                        : Collections.unmodifiableSet(
                        weapons);

        EnumSet<ArmorCategory> armor =
                EnumSet.noneOf(
                        ArmorCategory.class);

        if (armorProficiencies != null) {

            for (ArmorCategory category
                    : armorProficiencies) {

                if (category != null) {
                    armor.add(category);
                }
            }
        }

        armorProficiencies =
                armor.isEmpty()
                        ? Set.of()
                        : Collections.unmodifiableSet(
                        armor);
    }

    public static ProficiencySyncPayload current(
            ServerPlayer player) {

        if (player == null) {

            return new ProficiencySyncPayload(
                    Set.of(),
                    Set.of());
        }

        return new ProficiencySyncPayload(
                WeaponProficiencyService
                        .getProficiencies(player),
                ArmorProficiencyService
                        .getProficiencies(player));
    }

    public void write(
            FriendlyByteBuf buffer) {

        buffer.writeVarInt(
                weaponProficiencies.size());

        for (ResourceLocation id
                : weaponProficiencies) {

            buffer.writeResourceLocation(id);
        }

        buffer.writeVarInt(
                armorProficiencies.size());

        for (ArmorCategory category
                : armorProficiencies) {

            buffer.writeVarInt(
                    category.ordinal());
        }
    }

    public static ProficiencySyncPayload read(
            FriendlyByteBuf buffer) {

        int weaponCount =
                buffer.readVarInt();

        if (weaponCount < 0
                || weaponCount
                > MAX_WEAPON_PROFICIENCIES) {

            throw new IllegalArgumentException(
                    "Invalid weapon proficiency count: "
                            + weaponCount);
        }

        LinkedHashSet<ResourceLocation> weapons =
                new LinkedHashSet<>();

        for (int i = 0;
             i < weaponCount;
             i++) {

            weapons.add(
                    buffer.readResourceLocation());
        }

        int armorCount =
                buffer.readVarInt();

        if (armorCount < 0
                || armorCount
                > MAX_ARMOR_PROFICIENCIES) {

            throw new IllegalArgumentException(
                    "Invalid armor proficiency count: "
                            + armorCount);
        }

        ArmorCategory[] categories =
                ArmorCategory.values();

        EnumSet<ArmorCategory> armor =
                EnumSet.noneOf(
                        ArmorCategory.class);

        for (int i = 0;
             i < armorCount;
             i++) {

            int ordinal =
                    buffer.readVarInt();

            if (ordinal < 0
                    || ordinal
                    >= categories.length) {

                throw new IllegalArgumentException(
                        "Invalid armor category ordinal: "
                                + ordinal);
            }

            armor.add(
                    categories[ordinal]);
        }

        return new ProficiencySyncPayload(
                weapons,
                armor);
    }

    public void apply() {

        ProficiencyClientState.replace(
                weaponProficiencies,
                armorProficiencies);
    }
}