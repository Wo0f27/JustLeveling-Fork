package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.network.LockItemSyncPayload;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgeLockItemSyncPacket {
    private final List<String> lockItems;

    public ForgeLockItemSyncPacket(List<LockItem> lockItems) {
        this.lockItems = lockItems.stream().map(LockItem::toString).toList();
    }

    public ForgeLockItemSyncPacket(FriendlyByteBuf buffer) {
        this.lockItems = LockItemSyncPayload.read(buffer);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.lockItems.size());
        this.lockItems.forEach(buffer::writeUtf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> LockItemSyncPayload.apply(this.lockItems));
        context.setPacketHandled(true);
    }
}
