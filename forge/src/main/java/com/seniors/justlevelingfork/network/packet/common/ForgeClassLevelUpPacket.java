package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.player.ClassProgressionService;
import com.seniors.justlevelingfork.registry.RegistryClasses;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeClassLevelUpPacket {

    private final String classId;

    public ForgeClassLevelUpPacket(ResourceLocation classId) {
        this(classId.toString());
    }

    public ForgeClassLevelUpPacket(String classId) {
        this.classId = classId;
    }

    public ForgeClassLevelUpPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(classId);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            ResourceLocation resolvedClass =
                    RegistryClasses.get(classId);

            if (resolvedClass == null) {
                return;
            }

            ClassProgressionService.applyPendingLevelUp(
                    player,
                    resolvedClass);
        });

        context.setPacketHandled(true);
    }
}