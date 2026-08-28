package com.seniors.justlevelingfork.network.packet.client;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.network.ProficiencySyncPayload;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ForgeProficiencySyncPacket {

    private final ProficiencySyncPayload payload;

    public ForgeProficiencySyncPacket(
            ProficiencySyncPayload payload) {

        this.payload = payload;
    }

    public ForgeProficiencySyncPacket(
            FriendlyByteBuf buffer) {

        this(
                ProficiencySyncPayload.read(
                        buffer));
    }

    public void toBytes(
            FriendlyByteBuf buffer) {

        payload.write(buffer);
    }

    public void handle(
            Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context context =
                supplier.get();

        context.enqueueWork(() -> {

            payload.apply();

            /*
             * DEBUG-only diagnostic for this checkpoint.
             * This only fires when the server actually sends
             * a changed snapshot, not every polling interval.
             */
            Constants.LOG.info(
                    "CLIENT received effective proficiency snapshot. "
                            + "Weapons: {}, Armor: {}",
                    payload.weaponProficiencies(),
                    payload.armorProficiencies());
        });

        context.setPacketHandled(true);
    }
}