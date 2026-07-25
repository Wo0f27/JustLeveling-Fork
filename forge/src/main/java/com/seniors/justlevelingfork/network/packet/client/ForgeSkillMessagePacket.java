package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.common.player.SkillMessageService.SkillMessage;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ForgeSkillMessagePacket {
    private final String translationKey;
    private final int amount;

    public ForgeSkillMessagePacket(SkillMessage message) {
        this(message.translationKey(), message.amount());
    }

    public ForgeSkillMessagePacket(String translationKey, int amount) {
        this.translationKey = translationKey;
        this.amount = amount;
    }

    public ForgeSkillMessagePacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readInt());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.translationKey);
        buffer.writeInt(this.amount);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ForgeSkillMessageClientHandler.show(this.translationKey, this.amount)));
        context.setPacketHandled(true);
    }
}
