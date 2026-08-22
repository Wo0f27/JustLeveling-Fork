package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.player.CharacterAdminAction;
import com.seniors.justlevelingfork.common.player.CharacterAdminRequest;
import com.seniors.justlevelingfork.common.player.CharacterAdminService;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ForgeCharacterAdminActionPacket {

    private final CharacterAdminAction action;
    private final String classId;
    private final long amount;

    public ForgeCharacterAdminActionPacket(
            CharacterAdminRequest request) {

        this(
                request.action(),
                request.classId(),
                request.amount());
    }

    public ForgeCharacterAdminActionPacket(
            CharacterAdminAction action,
            String classId,
            long amount) {

        this.action = action;
        this.classId =
                classId == null
                        ? ""
                        : classId;
        this.amount = amount;
    }

    public ForgeCharacterAdminActionPacket(
            FriendlyByteBuf buffer) {

        this(
                buffer.readEnum(
                        CharacterAdminAction.class),
                buffer.readUtf(),
                buffer.readLong());
    }

    public void toBytes(
            FriendlyByteBuf buffer) {

        buffer.writeEnum(action);
        buffer.writeUtf(classId);
        buffer.writeLong(amount);
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

            /*
             * Every service method performs its own
             * permission-level-2 check.
             */
            switch (action) {

                case RESET_CHARACTER ->
                        CharacterAdminService
                                .resetCharacter(player);

                case SET_STARTING_CLASS -> {

                    ResourceLocation resolved =
                            ResourceLocation.tryParse(
                                    classId);

                    CharacterAdminService
                            .setStartingClass(
                                    player,
                                    resolved);
                }

                case APPLY_RECOMMENDED_ABILITIES ->
                        CharacterAdminService
                                .applyRecommendedAbilities(
                                        player);

                case ADD_XP ->
                        CharacterAdminService
                                .addXp(
                                        player,
                                        amount);

                case XP_TO_NEXT_LEVEL ->
                        CharacterAdminService
                                .addXpToNextLevel(
                                        player);
            }
        });

        context.setPacketHandled(true);
    }
}