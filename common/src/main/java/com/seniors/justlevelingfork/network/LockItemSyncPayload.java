package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public final class LockItemSyncPayload {
    private LockItemSyncPayload() {
    }

    public static void write(FriendlyByteBuf buffer, List<LockItem> lockItems) {
        buffer.writeInt(lockItems.size());
        lockItems.forEach(lockItem -> buffer.writeUtf(lockItem.toString()));
    }

    public static List<String> read(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<String> lockItems = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            lockItems.add(buffer.readUtf());
        }
        return lockItems;
    }

    public static void apply(List<String> lockItemEntries) {
        List<LockItem> lockItems = new ArrayList<>();
        for (String entry : lockItemEntries) {
            lockItems.add(LockItem.getLockItemFromString(entry, new LockItem()));
        }
        HandlerAptitude.updateLockItems(lockItems);
    }
}
