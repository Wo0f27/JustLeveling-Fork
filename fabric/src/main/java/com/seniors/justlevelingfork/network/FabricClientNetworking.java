package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.config.ClientConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.client.gui.ClientOverlayState;
import com.seniors.justlevelingfork.network.CommonConfigSyncPayload;
import com.seniors.justlevelingfork.network.LockItemSyncPayload;
import com.seniors.justlevelingfork.network.TitleDefinitionsSyncPayload;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FabricClientNetworking {
    private FabricClientNetworking() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(PlayerProgressNetwork.SYNC, (client, handler, buffer, responseSender) -> {
            CompoundTag tag = buffer.readNbt();
            client.execute(() -> PlayerProgressClientState.applySync(tag));
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerProgressNetwork.SKILL_MESSAGE, (client, handler, buffer, responseSender) -> {
            String translationKey = buffer.readUtf();
            int amount = buffer.readInt();
            client.execute(() -> {
                if (client.player != null && ClientConfigService.shouldShowSkillMessage(translationKey)) {
                    client.player.displayClientMessage(Component.translatable(translationKey, amount), true);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerProgressNetwork.APTITUDE_WARNING, (client, handler, buffer, responseSender) -> {
            String restrictionId = buffer.readUtf();
            client.execute(() -> ClientOverlayState.showAptitudeWarning(restrictionId));
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerProgressNetwork.TITLE_UNLOCK, (client, handler, buffer, responseSender) -> {
            String titleName = buffer.readUtf();
            client.execute(() -> ClientOverlayState.showTitleUnlock(titleName));
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerProgressNetwork.LOCK_ITEM_SYNC, (client, handler, buffer, responseSender) -> {
            var lockItems = LockItemSyncPayload.read(buffer);
            client.execute(() -> LockItemSyncPayload.apply(lockItems));
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerProgressNetwork.COMMON_CONFIG_SYNC, (client, handler, buffer, responseSender) -> {
            CommonConfigSyncPayload payload = CommonConfigSyncPayload.read(buffer);
            client.execute(payload::apply);
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerProgressNetwork.TITLE_DEFINITIONS_SYNC, (client, handler, buffer, responseSender) -> {
            TitleDefinitionsSyncPayload payload = TitleDefinitionsSyncPayload.read(buffer);
            client.execute(payload::apply);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                client.execute(RegistryTitles::clearClientTitleModels));

        PlayerProgressClientRequests.setAptitudeLevelUpSender(aptitudeName ->
                sendString(PlayerProgressNetwork.APTITUDE_LEVEL_UP, aptitudeName));
        PlayerProgressClientRequests.setPassiveLevelUpSender(passiveName ->
                sendString(PlayerProgressNetwork.PASSIVE_LEVEL_UP, passiveName));
        PlayerProgressClientRequests.setPassiveLevelDownSender(passiveName ->
                sendString(PlayerProgressNetwork.PASSIVE_LEVEL_DOWN, passiveName));
        PlayerProgressClientRequests.setPlayerTitleSender(titleName ->
                sendString(PlayerProgressNetwork.SET_PLAYER_TITLE, titleName));
        PlayerProgressClientRequests.setToggleSkillSender((skillName, enabled) ->
                sendSkillToggle(skillName, enabled));
        PlayerProgressClientRequests.setOpenEnderChestSender(() ->
                sendEmpty(PlayerProgressNetwork.OPEN_ENDER_CHEST));
    }

    private static void sendString(ResourceLocation channel, String value) {
        if (!ClientPlayNetworking.canSend(channel)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeUtf(value);
        ClientPlayNetworking.send(channel, buffer);
    }

    private static void sendSkillToggle(String skillName, boolean enabled) {
        if (!ClientPlayNetworking.canSend(PlayerProgressNetwork.TOGGLE_SKILL)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeUtf(skillName);
        buffer.writeBoolean(enabled);
        ClientPlayNetworking.send(PlayerProgressNetwork.TOGGLE_SKILL, buffer);
    }

    private static void sendEmpty(ResourceLocation channel) {
        if (ClientPlayNetworking.canSend(channel)) {
            ClientPlayNetworking.send(channel, PacketByteBufs.empty());
        }
    }
}
