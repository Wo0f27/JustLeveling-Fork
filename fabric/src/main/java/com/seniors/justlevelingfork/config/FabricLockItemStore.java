package com.seniors.justlevelingfork.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.HorseEquipmentRestrictions;
import com.seniors.justlevelingfork.common.command.RegisterItemCommand;
import com.seniors.justlevelingfork.config.DefaultLockItems;
import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricLockItemStore implements RegisterItemCommand.LockItemStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LOCK_ITEM_LIST = new TypeToken<List<LockItem>>() {}.getType();
    private static final FabricLockItemStore INSTANCE = new FabricLockItemStore();

    private final Path path = FabricLoader.getInstance().getConfigDir().resolve(Constants.CONFIG_NAME + ".lock_items.json");
    private List<LockItem> lockItems = new ArrayList<>();

    private FabricLockItemStore() {
    }

    public static FabricLockItemStore instance() {
        return INSTANCE;
    }

    public static void load() {
        INSTANCE.read();
        HandlerAptitude.setLockItems(INSTANCE::lockItems);
        HorseEquipmentRestrictions.setRestriction((player, stack) ->
                !(player instanceof ServerPlayer serverPlayer) || HandlerAptitude.canUseItem(serverPlayer, stack));
    }

    @Override
    public List<LockItem> lockItems() {
        return lockItems;
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(lockItems, LOCK_ITEM_LIST, writer);
            }
            HandlerAptitude.updateLockItems(lockItems);
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to save Fabric lock-item config {}", path, exception);
        }
    }

    public void reload() {
        read();
        HandlerAptitude.forceRefresh();
    }

    private void read() {
        if (!Files.exists(path)) {
            lockItems = DefaultLockItems.create();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            List<LockItem> loaded = GSON.fromJson(reader, LOCK_ITEM_LIST);
            lockItems = loaded == null ? new ArrayList<>() : new ArrayList<>(loaded);
            HandlerAptitude.updateLockItems(lockItems);
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to read Fabric lock-item config {}, using empty list", path, exception);
            lockItems = new ArrayList<>();
            HandlerAptitude.updateLockItems(lockItems);
        }
    }
}
