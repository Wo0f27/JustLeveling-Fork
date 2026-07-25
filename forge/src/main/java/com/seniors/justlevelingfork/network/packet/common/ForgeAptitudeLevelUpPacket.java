package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.player.PlayerProgressActions;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeAptitudeLevelUpPacket {
    private final String aptitudeName;

    public ForgeAptitudeLevelUpPacket(Aptitude aptitude) {
        this(aptitude.getName());
    }

    public ForgeAptitudeLevelUpPacket(String aptitudeName) {
        this.aptitudeName = aptitudeName;
    }

    public ForgeAptitudeLevelUpPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.aptitudeName);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PlayerProgressActions.levelUpAptitude(player, this.aptitudeName);
            }
        });
        context.setPacketHandled(true);
    }
}
