package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.player.PlayerProgressActions;
import com.seniors.justlevelingfork.registry.skills.Skill;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeToggleSkillPacket {
    private final String skillName;
    private final boolean enabled;

    public ForgeToggleSkillPacket(Skill skill, boolean enabled) {
        this(skill.getName(), enabled);
    }

    public ForgeToggleSkillPacket(String skillName, boolean enabled) {
        this.skillName = skillName;
        this.enabled = enabled;
    }

    public ForgeToggleSkillPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.skillName);
        buffer.writeBoolean(this.enabled);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PlayerProgressActions.setToggleSkill(player, this.skillName, this.enabled);
            }
        });
        context.setPacketHandled(true);
    }
}
