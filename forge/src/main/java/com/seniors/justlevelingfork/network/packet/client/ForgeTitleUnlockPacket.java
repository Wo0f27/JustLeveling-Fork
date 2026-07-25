package com.seniors.justlevelingfork.network.packet.client;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ForgeTitleUnlockPacket {
    private final String titleName;

    public ForgeTitleUnlockPacket(String titleName) {
        this.titleName = titleName;
    }

    public ForgeTitleUnlockPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.titleName);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ForgeTitleUnlockClientHandler.show(this.titleName)));
        context.setPacketHandled(true);
    }
}
