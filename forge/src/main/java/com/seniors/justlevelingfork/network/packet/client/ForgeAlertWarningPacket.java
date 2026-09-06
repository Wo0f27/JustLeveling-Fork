package com.seniors.justlevelingfork.network.packet.client;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ForgeAlertWarningPacket {

    private final int hostileCount;

    public ForgeAlertWarningPacket(int hostileCount) {
        this.hostileCount = Math.max(1, hostileCount);
    }

    public ForgeAlertWarningPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(hostileCount);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ForgeAlertWarningClientHandler.show(hostileCount)));
        context.setPacketHandled(true);
    }
}
