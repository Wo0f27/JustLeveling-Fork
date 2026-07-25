package com.seniors.justlevelingfork.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.config.SkillConfigService;
import com.seniors.justlevelingfork.common.config.SkillConfigService.SkillConfig;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricSkillConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SKILL_CONFIG_MAP = new TypeToken<Map<String, SkillConfig>>() {}.getType();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(Constants.CONFIG_NAME + ".skills.json");
    private static Map<String, SkillConfig> skillConfigs = SkillConfigService.defaultConfigs();

    private FabricSkillConfigStore() {
    }

    public static void load() {
        read();
        SkillConfigService.setSkillConfigs(() -> skillConfigs);
    }

    private static void read() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            Map<String, SkillConfig> loaded = GSON.fromJson(reader, SKILL_CONFIG_MAP);
            skillConfigs = SkillConfigService.sanitize(loaded == null ? SkillConfigService.defaultConfigs() : loaded);
        } catch (IOException | RuntimeException exception) {
            Constants.LOG.warn("Failed to read Fabric skill config {}, using defaults", PATH, exception);
            skillConfigs = SkillConfigService.defaultConfigs();
        }
        save();
    }

    private static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(skillConfigs, SKILL_CONFIG_MAP, writer);
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to create Fabric skill config {}", PATH, exception);
        }
    }
}
