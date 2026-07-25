package com.seniors.justlevelingfork.common.command;

import com.seniors.justlevelingfork.config.models.LockItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LockItemRegistration {
    private LockItemRegistration() {
    }

    public static Result apply(List<LockItem> lockItems, String itemId, String aptitudeName, int level) {
        Optional<LockItem> optionalLockItem = lockItems.stream()
                .filter(lockItem -> lockItem.Item.equalsIgnoreCase(itemId))
                .findFirst();

        if (optionalLockItem.isPresent()) {
            LockItem lockItem = optionalLockItem.get();
            int index = lockItems.indexOf(lockItem);
            lockItem.Aptitudes = new ArrayList<>(lockItem.Aptitudes);

            if (level < 1) {
                boolean removed = lockItem.Aptitudes.removeIf(aptitude ->
                        aptitude.Aptitude.toString().equalsIgnoreCase(aptitudeName));
                if (!removed) {
                    return Result.NO_MATCHING_APTITUDE;
                }
                if (lockItem.Aptitudes.isEmpty()) {
                    lockItems.remove(index);
                    return Result.REMOVED_ITEM;
                }
                lockItems.set(index, lockItem);
                return Result.REMOVED_APTITUDE;
            }

            lockItem.Aptitudes.removeIf(aptitude ->
                    aptitude.Aptitude.toString().equalsIgnoreCase(aptitudeName));
            lockItem.Aptitudes.add(new LockItem.Aptitude(aptitudeName, level));
            lockItems.set(index, lockItem);
            return Result.UPDATED_ITEM;
        }

        if (level < 1) {
            return Result.NO_MATCHING_ITEM;
        }

        LockItem lockItem = new LockItem(itemId);
        lockItem.Aptitudes = new ArrayList<>();
        lockItem.Aptitudes.add(new LockItem.Aptitude(aptitudeName, level));
        lockItems.add(lockItem);
        return Result.ADDED_ITEM;
    }

    public enum Result {
        ADDED_ITEM("Item added into lockItemList...", true),
        UPDATED_ITEM("Item already in lockItemList, adding extra aptitude...", true),
        REMOVED_ITEM("Removing item from lockItemList...", true),
        REMOVED_APTITUDE("Removing aptitude from item...", true),
        NO_MATCHING_ITEM("Item is not in lockItemList.", false),
        NO_MATCHING_APTITUDE("Aptitude is not registered for item.", false);

        private final String message;
        private final boolean changed;

        Result(String message, boolean changed) {
            this.message = message;
            this.changed = changed;
        }

        public String message() {
            return message;
        }

        public boolean changed() {
            return changed;
        }
    }
}
