package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.network.FeatDefinitionsSyncPayload;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgeFeatDefinitionsSyncPacket {

    private final FeatDefinitionsSyncPayload payload;

    public ForgeFeatDefinitionsSyncPacket(
            FeatDefinitionsSyncPayload payload) {

        this.payload = payload;
    }

    public ForgeFeatDefinitionsSyncPacket(
            FriendlyByteBuf buffer) {

        this(
                FeatDefinitionsSyncPayload.read(
                        buffer));
    }

    public void toBytes(
            FriendlyByteBuf buffer) {

        payload.write(buffer);
    }

    public void handle(
            Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context context =
                supplier.get();

        context.enqueueWork(
                payload::apply);

        context.setPacketHandled(true);
    }
}