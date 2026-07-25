package com.seniors.justlevelingfork.common.config;

import com.seniors.justlevelingfork.config.models.TitleModel;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class TitleConfigService {
    private static Supplier<Collection<TitleModel>> titleModels = RegistryTitles::defaultModels;

    private TitleConfigService() {
    }

    public static void setTitleModels(Supplier<Collection<TitleModel>> titleModels) {
        TitleConfigService.titleModels = Optional.ofNullable(titleModels).orElse(RegistryTitles::defaultModels);
        RegistryTitles.reload();
    }

    public static Collection<TitleModel> titleModels() {
        Collection<TitleModel> models = titleModels.get();
        if (models == null || models.isEmpty()) {
            return RegistryTitles.defaultModels();
        }
        return List.copyOf(models);
    }
}
