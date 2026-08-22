package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.common.player.CharacterAdminClientState;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgeCharacterAdminAccessSyncPacket {

    private final boolean allowed;

    public ForgeCharacterAdminAccessSyncPacket(
            boolean allowed) {

        this.allowed = allowed;
    }

    public ForgeCharacterAdminAccessSyncPacket(
            FriendlyByteBuf buffer) {

        this(buffer.readBoolean());
    }

    public void toBytes(
            FriendlyByteBuf buffer) {

        buffer.writeBoolean(allowed);
    }

    public void handle(
            Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context context =
                supplier.get();

        context.enqueueWork(() ->
                CharacterAdminClientState
                        .setAllowed(allowed));

        context.setPacketHandled(true);
    }
}