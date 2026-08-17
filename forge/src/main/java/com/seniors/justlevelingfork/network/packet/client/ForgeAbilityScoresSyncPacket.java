package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.network.AbilityScoresSyncPayload;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgeAbilityScoresSyncPacket {

    private final AbilityScoresSyncPayload payload;

    public ForgeAbilityScoresSyncPacket(
            AbilityScoresSyncPayload payload) {

        this.payload = payload;
    }

    public ForgeAbilityScoresSyncPacket(
            FriendlyByteBuf buffer) {

        this(
                AbilityScoresSyncPayload.read(
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