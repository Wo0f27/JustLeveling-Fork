package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.player.PlayerProgressActions;
import com.seniors.justlevelingfork.registry.passive.Passive;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgePassiveLevelDownPacket {
    private final String passiveName;

    public ForgePassiveLevelDownPacket(Passive passive) {
        this(passive.getName());
    }

    public ForgePassiveLevelDownPacket(String passiveName) {
        this.passiveName = passiveName;
    }

    public ForgePassiveLevelDownPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.passiveName);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PlayerProgressActions.levelDownPassive(player, this.passiveName);
            }
        });
        context.setPacketHandled(true);
    }
}
