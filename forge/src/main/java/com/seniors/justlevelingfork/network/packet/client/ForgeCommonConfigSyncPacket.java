package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.network.CommonConfigSyncPayload;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgeCommonConfigSyncPacket {
    private final CommonConfigSyncPayload payload;

    public ForgeCommonConfigSyncPacket(CommonConfigSyncPayload payload) {
        this.payload = payload;
    }

    public ForgeCommonConfigSyncPacket(FriendlyByteBuf buffer) {
        this(CommonConfigSyncPayload.read(buffer));
    }

    public void toBytes(FriendlyByteBuf buffer) {
        this.payload.write(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(this.payload::apply);
        context.setPacketHandled(true);
    }
}
