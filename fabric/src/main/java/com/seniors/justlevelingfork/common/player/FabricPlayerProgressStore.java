package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.config.FabricLockItemStore;
import com.seniors.justlevelingfork.network.CommonConfigSyncPayload;
import com.seniors.justlevelingfork.network.LockItemSyncPayload;
import com.seniors.justlevelingfork.network.PlayerProgressNetwork;
import com.seniors.justlevelingfork.network.TitleDefinitionsSyncPayload;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

public final class FabricPlayerProgressStore {
    private static final String DATA_NAME = Constants.MOD_ID + "_player_progress";

    private FabricPlayerProgressStore() {
    }

    public static void load() {
        PlayerProgressService.setProgressProvider(FabricPlayerProgressStore::get);
        PlayerProgressService.setChangeListener((player, progress) -> data(player).setDirty());
        PlayerProgressService.setSyncHandler(FabricPlayerProgressStore::sync);
        AptitudeWarningService.setSender(FabricPlayerProgressStore::sendAptitudeWarning);
        SkillMessageService.setSender(FabricPlayerProgressStore::sendSkillMessage);
        TitleUnlockService.setSender(FabricPlayerProgressStore::sendTitleUnlock);
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
                get(oldPlayer).ifPresent(oldProgress -> get(newPlayer).ifPresent(newProgress ->
                        {
                            newProgress.copyFrom(oldProgress);
                            PlayerProgressService.refreshPassiveModifiers(newPlayer);
                            PlayerProgressService.sync(newPlayer);
                            data(newPlayer).setDirty();
                        })));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerProgressService.refreshPassiveModifiers(handler.player);
            syncTitleDefinitions(handler.player);
            PlayerProgressService.sync(handler.player);
            syncLockItems(handler.player);
            syncCommonConfig(handler.player);
        });
    }

    public static Optional<PlayerProgress> get(ServerPlayer player) {
        if (player == null) {
            return Optional.empty();
        }

        FabricPlayerProgressData data = data(player);
        boolean existed = data.contains(player.getUUID());
        PlayerProgress progress = data.getOrCreate(player.getUUID());
        if (!existed) {
            data.setDirty();
        }
        return Optional.of(progress);
    }

    private static FabricPlayerProgressData data(ServerPlayer player) {
        return player.server.overworld()
                .getDataStorage()
                .computeIfAbsent(FabricPlayerProgressData::load, FabricPlayerProgressData::new, DATA_NAME);
    }

    private static void sync(ServerPlayer player, PlayerProgress progress) {
        if (!ServerPlayNetworking.canSend(player, PlayerProgressNetwork.SYNC)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeNbt(progress.serializeNBT());
        ServerPlayNetworking.send(player, PlayerProgressNetwork.SYNC, buffer);
    }

    public static void syncLockItems(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, PlayerProgressNetwork.LOCK_ITEM_SYNC)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        LockItemSyncPayload.write(buffer, FabricLockItemStore.instance().lockItems());
        ServerPlayNetworking.send(player, PlayerProgressNetwork.LOCK_ITEM_SYNC, buffer);
    }

    public static void syncTitleDefinitions(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, PlayerProgressNetwork.TITLE_DEFINITIONS_SYNC)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        TitleDefinitionsSyncPayload.current().write(buffer);
        ServerPlayNetworking.send(player, PlayerProgressNetwork.TITLE_DEFINITIONS_SYNC, buffer);
    }

    public static void syncCommonConfig(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, PlayerProgressNetwork.COMMON_CONFIG_SYNC)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        CommonConfigSyncPayload.current().write(buffer);
        ServerPlayNetworking.send(player, PlayerProgressNetwork.COMMON_CONFIG_SYNC, buffer);
    }

    private static void sendSkillMessage(ServerPlayer player, SkillMessageService.SkillMessage message) {
        if (!ServerPlayNetworking.canSend(player, PlayerProgressNetwork.SKILL_MESSAGE)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeUtf(message.translationKey());
        buffer.writeInt(message.amount());
        ServerPlayNetworking.send(player, PlayerProgressNetwork.SKILL_MESSAGE, buffer);
    }

    private static void sendAptitudeWarning(ServerPlayer player, String restrictionId) {
        if (!ServerPlayNetworking.canSend(player, PlayerProgressNetwork.APTITUDE_WARNING)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeUtf(restrictionId);
        ServerPlayNetworking.send(player, PlayerProgressNetwork.APTITUDE_WARNING, buffer);
    }

    private static void sendTitleUnlock(ServerPlayer player, String titleName) {
        if (!ServerPlayNetworking.canSend(player, PlayerProgressNetwork.TITLE_UNLOCK)) {
            return;
        }

        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeUtf(titleName);
        ServerPlayNetworking.send(player, PlayerProgressNetwork.TITLE_UNLOCK, buffer);
    }

    private static PlayerProgress createProgress() {
        return PlayerProgress.withNames(
                RegistryPassives.values().stream().map(passive -> passive.getName()).toList(),
                RegistrySkills.values().stream().map(skill -> skill.getName()).toList(),
                RegistryTitles.defaults());
    }

    private static final class FabricPlayerProgressData extends SavedData {
        private final Map<UUID, PlayerProgress> progressByPlayer = new ConcurrentHashMap<>();

        private static FabricPlayerProgressData load(CompoundTag tag) {
            FabricPlayerProgressData data = new FabricPlayerProgressData();
            CompoundTag players = tag.getCompound("players");
            for (String key : players.getAllKeys()) {
                try {
                    UUID playerId = UUID.fromString(key);
                    PlayerProgress progress = createProgress();
                    progress.deserializeNBT(players.getCompound(key));
                    data.progressByPlayer.put(playerId, progress);
                } catch (IllegalArgumentException exception) {
                    Constants.LOG.warn("Ignoring invalid Fabric player progress UUID '{}'", key);
                }
            }
            return data;
        }

        private PlayerProgress getOrCreate(UUID playerId) {
            return progressByPlayer.computeIfAbsent(playerId, ignored -> createProgress());
        }

        private boolean contains(UUID playerId) {
            return progressByPlayer.containsKey(playerId);
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            CompoundTag players = new CompoundTag();
            progressByPlayer.forEach((playerId, progress) ->
                    players.put(playerId.toString(), progress.serializeNBT()));
            tag.put("players", players);
            return tag;
        }
    }
}
