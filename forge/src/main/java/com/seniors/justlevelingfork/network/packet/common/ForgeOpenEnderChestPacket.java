package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.player.PlayerProgressActions;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeOpenEnderChestPacket {
    public ForgeOpenEnderChestPacket() {
    }

    public ForgeOpenEnderChestPacket(FriendlyByteBuf buffer) {
    }

    public void toBytes(FriendlyByteBuf buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PlayerProgressActions.openEnderChest(player);
            }
        });
        context.setPacketHandled(true);
    }
}
