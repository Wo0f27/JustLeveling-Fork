package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public final class LockItemSyncPayload {
    private static final int MAX_LOCK_ITEMS = 1024;
    private static final int MAX_LOCK_ITEM_LENGTH = 8192;

    private LockItemSyncPayload() {
    }

    public static void write(FriendlyByteBuf buffer, List<LockItem> lockItems) {
        buffer.writeInt(lockItems.size());
        lockItems.forEach(lockItem -> buffer.writeUtf(lockItem.toString()));
    }

    public static List<String> read(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        requireCount(size, MAX_LOCK_ITEMS, "lock items");
        List<String> lockItems = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            lockItems.add(buffer.readUtf(MAX_LOCK_ITEM_LENGTH));
        }
        return lockItems;
    }

    public static void apply(List<String> lockItemEntries) {
        List<LockItem> lockItems = new ArrayList<>();
        for (String entry : lockItemEntries) {
            LockItem lockItem = LockItem.getLockItemFromString(entry, null);
            if (lockItem != null) {
                lockItems.add(lockItem);
            }
        }
        HandlerAptitude.updateLockItems(LockItem.sanitizedList(lockItems));
    }

    private static void requireCount(int count, int maximum, String description) {
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("Invalid " + description + " count: " + count);
        }
    }
}
