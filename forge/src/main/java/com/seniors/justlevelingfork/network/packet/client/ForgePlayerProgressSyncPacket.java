package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgePlayerProgressSyncPacket {
    private final CompoundTag tag;

    public ForgePlayerProgressSyncPacket(PlayerProgress progress) {
        this(progress.serializeNBT());
    }

    public ForgePlayerProgressSyncPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public ForgePlayerProgressSyncPacket(FriendlyByteBuf buffer) {
        this(buffer.readNbt());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeNbt(this.tag);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> PlayerProgressClientState.applySync(this.tag));
        context.setPacketHandled(true);
    }
}
