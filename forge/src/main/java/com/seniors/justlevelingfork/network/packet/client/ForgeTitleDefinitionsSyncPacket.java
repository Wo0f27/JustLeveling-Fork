package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.network.TitleDefinitionsSyncPayload;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgeTitleDefinitionsSyncPacket {
    private final TitleDefinitionsSyncPayload payload;

    public ForgeTitleDefinitionsSyncPacket(TitleDefinitionsSyncPayload payload) {
        this.payload = payload;
    }

    public ForgeTitleDefinitionsSyncPacket(FriendlyByteBuf buffer) {
        this(TitleDefinitionsSyncPayload.read(buffer));
    }

    public void toBytes(FriendlyByteBuf buffer) {
        payload.write(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(payload::apply);
        context.setPacketHandled(true);
    }
}
