package com.seniors.justlevelingfork.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.config.PassiveConfigService;
import com.seniors.justlevelingfork.common.config.PassiveConfigService.PassiveConfig;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricPassiveConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type PASSIVE_CONFIG_MAP = new TypeToken<Map<String, PassiveConfig>>() {}.getType();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(Constants.CONFIG_NAME + ".passives.json");
    private static Map<String, PassiveConfig> passiveConfigs = PassiveConfigService.defaultConfigs();

    private FabricPassiveConfigStore() {
    }

    public static void load() {
        read();
        PassiveConfigService.setPassiveConfigs(() -> passiveConfigs);
    }

    private static void read() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            Map<String, PassiveConfig> loaded = GSON.fromJson(reader, PASSIVE_CONFIG_MAP);
            passiveConfigs = PassiveConfigService.sanitize(loaded == null ? PassiveConfigService.defaultConfigs() : loaded);
        } catch (IOException | RuntimeException exception) {
            Constants.LOG.warn("Failed to read Fabric passive config {}, using defaults", PATH, exception);
            passiveConfigs = PassiveConfigService.defaultConfigs();
        }
        save();
    }

    private static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(passiveConfigs, PASSIVE_CONFIG_MAP, writer);
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to create Fabric passive config {}", PATH, exception);
        }
    }
}
