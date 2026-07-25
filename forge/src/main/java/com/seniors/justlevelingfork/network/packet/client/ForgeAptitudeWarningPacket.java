package com.seniors.justlevelingfork.network.packet.client;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ForgeAptitudeWarningPacket {
    private final String restrictionId;

    public ForgeAptitudeWarningPacket(String restrictionId) {
        this.restrictionId = restrictionId;
    }

    public ForgeAptitudeWarningPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.restrictionId);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ForgeAptitudeWarningClientHandler.show(this.restrictionId)));
        context.setPacketHandled(true);
    }
}
