package com.seniors.justlevelingfork.config.models;

import com.seniors.justlevelingfork.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public class LockItem {
    private static final String DROPPABLE_MARKER = "<droppable>";

    public String Item = "minecraft:diamond";

    public List<Aptitude> Aptitudes = List.of(new Aptitude());
    public boolean Droppable = false;

    public LockItem() {
    }

    public LockItem(String itemName) {
        Item = itemName;
    }

    public LockItem(String itemName, Aptitude... aptitudes) {
        Item = itemName;
        Aptitudes = Arrays.stream(aptitudes).toList();
    }

    public static LockItem getLockItemFromString(String value, LockItem defaultValue) {
        try {
            return formatString(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Returns a copy of the usable lock-item entries in a user-supplied config.
     * JSON configs are edited by hand, so never let a partial entry reach the
     * runtime restriction map.
     */
    public static List<LockItem> sanitizedList(List<LockItem> lockItems) {
        List<LockItem> sanitized = new ArrayList<>();
        if (lockItems == null) {
            return sanitized;
        }

        for (LockItem lockItem : lockItems) {
            if (lockItem == null || !isValidItemId(lockItem.Item) || lockItem.Aptitudes == null) {
                continue;
            }

            List<Aptitude> aptitudes = new ArrayList<>();
            for (Aptitude aptitude : lockItem.Aptitudes) {
                if (aptitude == null || aptitude.Aptitude == null || aptitude.Level < 2 || aptitude.Level > 1000) {
                    continue;
                }
                Aptitude copy = new Aptitude();
                copy.Aptitude = aptitude.Aptitude;
                copy.Level = aptitude.Level;
                aptitudes.add(copy);
            }

            if (aptitudes.isEmpty()) {
                continue;
            }

            LockItem copy = new LockItem(lockItem.Item);
            copy.Droppable = lockItem.Droppable;
            copy.Aptitudes = aptitudes;
            sanitized.add(copy);
        }
        return sanitized;
    }

    private static boolean isValidItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }
        try {
            new ResourceLocation(itemId);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static LockItem formatString(String value) {
        String[] initialSplit = value.split("#");
        LockItem lockItem = new LockItem(initialSplit[0]);
        lockItem.Droppable = initialSplit[1].contains(DROPPABLE_MARKER);

        List<Aptitude> aptitudeList = new ArrayList<>();
        String[] aptitudeSplit = initialSplit[1].replace(DROPPABLE_MARKER, "").split(";");

        for (String aptitudeString : aptitudeSplit) {
            String[] aptitude = aptitudeString.split(":");
            int level = Integer.parseInt(aptitude[1]);
            if (level < 2 || level > 1000) {
                throw new IndexOutOfBoundsException();
            }
            aptitudeList.add(new Aptitude(aptitude[0], level));
        }

        lockItem.Aptitudes = aptitudeList;
        return lockItem;
    }

    @Override
    public String toString() {
        List<Aptitude> aptitudes = Aptitudes == null ? List.of() : Aptitudes;
        if (aptitudes.stream().anyMatch(Objects::isNull)) {
            Constants.LOG.info(">> Found null aptitude at item {}", this.Item);
        }
        List<String> strings = new ArrayList<>();
        try {
            strings = aptitudes.stream().filter(Objects::nonNull).map(Aptitude::toString).toList();
        } catch (NullPointerException e) {
            Constants.LOG.info(">> Found null aptitude at item {}", this.Item);
        }

        String aptitudeStringList = String.join(";", strings);

        return String.format("%s#%s%s", Item, aptitudeStringList, Droppable ? DROPPABLE_MARKER : "");
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    public static class Aptitude {
        public EAptitude Aptitude;

        public int Level;

        public Aptitude(String aptitudeName, int level) {
            try {
                Aptitude = EAptitude.valueOf(capitalize(aptitudeName));
            } catch (IllegalArgumentException e) {
                Constants.LOG.info(">> Wrong aptitude name {}", aptitudeName);
                Aptitude = EAptitude.Strength;
            }
            Level = level;
        }

        public Aptitude() {
            Aptitude = EAptitude.Strength;
            Level = 2;
        }

        @Override
        public String toString() {
            return String.format("%s:%d", Aptitude.toString(), Level);
        }
    }
}
