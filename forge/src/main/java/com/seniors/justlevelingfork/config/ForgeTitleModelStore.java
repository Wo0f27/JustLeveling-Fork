package com.seniors.justlevelingfork.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.config.TitleConfigService;
import com.seniors.justlevelingfork.config.models.TitleModel;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ForgeTitleModelStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TITLE_MODEL_LIST = new TypeToken<List<TitleModel>>() {}.getType();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve(Constants.CONFIG_NAME + ".titles.json");
    private static List<TitleModel> titleModels = new ArrayList<>(RegistryTitles.defaultModels());

    private ForgeTitleModelStore() {
    }

    public static void load() {
        read();
        TitleConfigService.setTitleModels(() -> titleModels);
    }

    public static void reload() {
        read();
        TitleConfigService.setTitleModels(() -> titleModels);
    }

    private static void read() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            List<TitleModel> loaded = GSON.fromJson(reader, TITLE_MODEL_LIST);
            titleModels = loaded == null || loaded.isEmpty()
                    ? new ArrayList<>(RegistryTitles.defaultModels())
                    : new ArrayList<>(loaded);
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to read Forge title config {}, using defaults", PATH, exception);
            titleModels = new ArrayList<>(RegistryTitles.defaultModels());
        }
    }

    private static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(titleModels, TITLE_MODEL_LIST, writer);
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to create Forge title config {}", PATH, exception);
        }
    }
}
