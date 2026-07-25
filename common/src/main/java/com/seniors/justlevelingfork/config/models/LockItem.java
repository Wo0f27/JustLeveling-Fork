package com.seniors.justlevelingfork.config.models;

import com.seniors.justlevelingfork.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        if (Aptitudes.stream().anyMatch(Objects::isNull)) {
            Constants.LOG.info(">> Found null aptitude at item {}", this.Item);
        }
        List<String> strings = new ArrayList<>();
        try {
            strings = Aptitudes.stream().map(Aptitude::toString).toList();
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
