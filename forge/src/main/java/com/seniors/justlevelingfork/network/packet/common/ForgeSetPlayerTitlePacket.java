package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.player.PlayerProgressActions;
import com.seniors.justlevelingfork.registry.title.Title;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeSetPlayerTitlePacket {
    private final String titleName;

    public ForgeSetPlayerTitlePacket(Title title) {
        this(title.getName());
    }

    public ForgeSetPlayerTitlePacket(String titleName) {
        this.titleName = titleName;
    }

    public ForgeSetPlayerTitlePacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.titleName);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PlayerProgressActions.setPlayerTitle(player, this.titleName);
            }
        });
        context.setPacketHandled(true);
    }
}
