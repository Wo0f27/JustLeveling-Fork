package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.network.ForgeServerNetworking;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeCharacterAdminAccessRequestPacket {

    public ForgeCharacterAdminAccessRequestPacket() {
    }

    public ForgeCharacterAdminAccessRequestPacket(
            FriendlyByteBuf buffer) {
    }

    public void toBytes(
            FriendlyByteBuf buffer) {
    }

    public void handle(
            Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context context =
                supplier.get();

        context.enqueueWork(() -> {

            ServerPlayer player =
                    context.getSender();

            if (player == null) {
                return;
            }

            ForgeServerNetworking
                    .syncCharacterAdminAccess(
                            player,
                            player.hasPermissions(2));
        });

        context.setPacketHandled(true);
    }
}