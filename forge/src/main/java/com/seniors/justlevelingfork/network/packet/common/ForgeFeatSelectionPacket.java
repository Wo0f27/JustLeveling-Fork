package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeFeatSelectionPacket {

    private final String featId;
    private final String choice;

    public ForgeFeatSelectionPacket(
            String featId,
            String choice) {

        this.featId =
                featId == null ? "" : featId;

        this.choice =
                choice == null ? "" : choice;
    }

    public ForgeFeatSelectionPacket(
            FriendlyByteBuf buffer) {

        this(
                buffer.readUtf(256),
                buffer.readUtf(256));
    }

    public void toBytes(
            FriendlyByteBuf buffer) {

        buffer.writeUtf(
                featId,
                256);

        buffer.writeUtf(
                choice,
                256);
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

            ResourceLocation resolvedFeat =
                    ResourceLocation.tryParse(
                            featId);

            if (resolvedFeat == null) {
                return;
            }

            /*
             * Server performs all real validation:
             *
             * - feat exists
             * - advancement available
             * - minimum level
             * - rank limits
             * - effect exists
             * - effect-specific choice validity
             */
            FeatProgressionService.takeFeat(
                    player,
                    resolvedFeat,
                    choice);
        });

        context.setPacketHandled(true);
    }
}