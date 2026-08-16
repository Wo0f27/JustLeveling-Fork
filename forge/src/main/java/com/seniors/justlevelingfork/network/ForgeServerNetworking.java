package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.AptitudeWarningService;
import com.seniors.justlevelingfork.common.player.SkillMessageService;
import com.seniors.justlevelingfork.common.player.SkillMessageService.SkillMessage;
import com.seniors.justlevelingfork.common.player.TitleUnlockService;
import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.network.CommonConfigSyncPayload;
import com.seniors.justlevelingfork.network.packet.client.ForgeAptitudeWarningPacket;
import com.seniors.justlevelingfork.network.packet.client.ForgeCommonConfigSyncPacket;
import com.seniors.justlevelingfork.network.packet.client.ForgeLockItemSyncPacket;
import com.seniors.justlevelingfork.network.packet.client.ForgePlayerProgressSyncPacket;
import com.seniors.justlevelingfork.network.packet.client.ForgeSkillMessagePacket;
import com.seniors.justlevelingfork.network.packet.client.ForgeTitleUnlockPacket;
import com.seniors.justlevelingfork.network.packet.client.ForgeTitleDefinitionsSyncPacket;
import com.seniors.justlevelingfork.network.packet.common.ForgeAptitudeLevelUpPacket;
import com.seniors.justlevelingfork.network.packet.common.ForgeOpenEnderChestPacket;
import com.seniors.justlevelingfork.network.packet.common.ForgePassiveLevelDownPacket;
import com.seniors.justlevelingfork.network.packet.common.ForgePassiveLevelUpPacket;
import com.seniors.justlevelingfork.network.packet.common.ForgeSetPlayerTitlePacket;
import com.seniors.justlevelingfork.network.packet.common.ForgeToggleSkillPacket;
import java.util.List;
import java.util.Optional;
import com.seniors.justlevelingfork.network.TitleDefinitionsSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import com.seniors.justlevelingfork.network.packet.common.ForgeClassLevelUpPacket;
import com.seniors.justlevelingfork.network.FeatDefinitionsSyncPayload;
import com.seniors.justlevelingfork.network.packet.client.ForgeFeatDefinitionsSyncPacket;
import com.seniors.justlevelingfork.network.packet.common.ForgeFeatSelectionPacket;


public final class ForgeServerNetworking {
    // Version 2 adds the server-authoritative title-definition packet. Keeping
    // the old version would let 1.2.5 clients connect with incompatible packet ids.
    private static final String PROTOCOL_VERSION = "6";

    private static int packetId;
    private static SimpleChannel channel;

    private ForgeServerNetworking() {
    }

    public static void init() {
        channel = NetworkRegistry.ChannelBuilder.named(PlayerProgressNetwork.CHANNEL)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        channel.registerMessage(
                packetId++,
                ForgePlayerProgressSyncPacket.class,
                ForgePlayerProgressSyncPacket::toBytes,
                ForgePlayerProgressSyncPacket::new,
                ForgePlayerProgressSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeFeatDefinitionsSyncPacket.class,
                ForgeFeatDefinitionsSyncPacket::toBytes,
                ForgeFeatDefinitionsSyncPacket::new,
                ForgeFeatDefinitionsSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeSkillMessagePacket.class,
                ForgeSkillMessagePacket::toBytes,
                ForgeSkillMessagePacket::new,
                ForgeSkillMessagePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeAptitudeWarningPacket.class,
                ForgeAptitudeWarningPacket::toBytes,
                ForgeAptitudeWarningPacket::new,
                ForgeAptitudeWarningPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeTitleUnlockPacket.class,
                ForgeTitleUnlockPacket::toBytes,
                ForgeTitleUnlockPacket::new,
                ForgeTitleUnlockPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeLockItemSyncPacket.class,
                ForgeLockItemSyncPacket::toBytes,
                ForgeLockItemSyncPacket::new,
                ForgeLockItemSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeCommonConfigSyncPacket.class,
                ForgeCommonConfigSyncPacket::toBytes,
                ForgeCommonConfigSyncPacket::new,
                ForgeCommonConfigSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeTitleDefinitionsSyncPacket.class,
                ForgeTitleDefinitionsSyncPacket::toBytes,
                ForgeTitleDefinitionsSyncPacket::new,
                ForgeTitleDefinitionsSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(
                packetId++,
                ForgeAptitudeLevelUpPacket.class,
                ForgeAptitudeLevelUpPacket::toBytes,
                ForgeAptitudeLevelUpPacket::new,
                ForgeAptitudeLevelUpPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(
                packetId++,
                ForgePassiveLevelUpPacket.class,
                ForgePassiveLevelUpPacket::toBytes,
                ForgePassiveLevelUpPacket::new,
                ForgePassiveLevelUpPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(
                packetId++,
                ForgeFeatSelectionPacket.class,
                ForgeFeatSelectionPacket::toBytes,
                ForgeFeatSelectionPacket::new,
                ForgeFeatSelectionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(
                packetId++,
                ForgePassiveLevelDownPacket.class,
                ForgePassiveLevelDownPacket::toBytes,
                ForgePassiveLevelDownPacket::new,
                ForgePassiveLevelDownPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(
                packetId++,
                ForgeSetPlayerTitlePacket.class,
                ForgeSetPlayerTitlePacket::toBytes,
                ForgeSetPlayerTitlePacket::new,
                ForgeSetPlayerTitlePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(
                packetId++,
                ForgeToggleSkillPacket.class,
                ForgeToggleSkillPacket::toBytes,
                ForgeToggleSkillPacket::new,
                ForgeToggleSkillPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(
                packetId++,
                ForgeOpenEnderChestPacket.class,
                ForgeOpenEnderChestPacket::toBytes,
                ForgeOpenEnderChestPacket::new,
                ForgeOpenEnderChestPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(
                packetId++,
                ForgeClassLevelUpPacket.class,
                ForgeClassLevelUpPacket::toBytes,
                ForgeClassLevelUpPacket::new,
                ForgeClassLevelUpPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        PlayerProgressClientRequests.setAptitudeLevelUpSender(aptitudeName ->
                sendToServer(new ForgeAptitudeLevelUpPacket(aptitudeName)));
        PlayerProgressClientRequests.setFeatSelectionSender((featId, choice) ->
                sendToServer(new ForgeFeatSelectionPacket(featId, choice)));
        PlayerProgressClientRequests.setPassiveLevelUpSender(passiveName ->
                sendToServer(new ForgePassiveLevelUpPacket(passiveName)));
        PlayerProgressClientRequests.setPassiveLevelDownSender(passiveName ->
                sendToServer(new ForgePassiveLevelDownPacket(passiveName)));
        PlayerProgressClientRequests.setPlayerTitleSender(titleName ->
                sendToServer(new ForgeSetPlayerTitlePacket(titleName)));
        PlayerProgressClientRequests.setToggleSkillSender((skillName, enabled) ->
                sendToServer(new ForgeToggleSkillPacket(skillName, enabled)));
        PlayerProgressClientRequests.setOpenEnderChestSender(() ->
                sendToServer(new ForgeOpenEnderChestPacket()));
        PlayerProgressClientRequests.setClassLevelUpSender(classId ->
                sendToServer(new ForgeClassLevelUpPacket(classId)));
        AptitudeWarningService.setSender(ForgeServerNetworking::sendAptitudeWarning);
        SkillMessageService.setSender(ForgeServerNetworking::sendSkillMessage);
        TitleUnlockService.setSender(ForgeServerNetworking::sendTitleUnlock);
    }

    public static void sendToServer(Object message) {
        channel.sendToServer(message);
    }

    public static void syncPlayerProgress(ServerPlayer player, PlayerProgress progress) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ForgePlayerProgressSyncPacket(progress));
    }
    public static void syncFeatDefinitions(ServerPlayer player) {if (player == null) {return;}
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ForgeFeatDefinitionsSyncPacket(FeatDefinitionsSyncPayload.current()));}

    public static void syncLockItems(ServerPlayer player, List<LockItem> lockItems) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ForgeLockItemSyncPacket(lockItems));
    }

    public static void syncCommonConfig(ServerPlayer player) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ForgeCommonConfigSyncPacket(CommonConfigSyncPayload.current()));
    }

    public static void syncTitleDefinitions(ServerPlayer player) {
        channel.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ForgeTitleDefinitionsSyncPacket(TitleDefinitionsSyncPayload.current()));
    }

    private static void sendSkillMessage(ServerPlayer player, SkillMessage message) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ForgeSkillMessagePacket(message));
    }

    private static void sendAptitudeWarning(ServerPlayer player, String restrictionId) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ForgeAptitudeWarningPacket(restrictionId));
    }

    private static void sendTitleUnlock(ServerPlayer player, String titleName) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ForgeTitleUnlockPacket(titleName));
    }
}
