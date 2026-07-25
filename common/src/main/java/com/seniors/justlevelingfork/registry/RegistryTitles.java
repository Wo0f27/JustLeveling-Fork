package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.config.TitleConfigService;
import com.seniors.justlevelingfork.config.models.TitleModel;
import com.seniors.justlevelingfork.registry.title.Title;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistryTitles {
    public static final ResourceKey<Registry<Title>> TITLES_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, "titles"));

    public static final Title TITLELESS = register("titleless", true);
    public static final Title ADMIN = register("administrator", false);

    private static final Map<String, Title> TITLES_BY_NAME = new LinkedHashMap<>();
    private static Collection<TitleModel> clientTitleModels;

    private RegistryTitles() {
    }

    public static Collection<Title> defaults() {
        ensureLoaded();
        return TITLES_BY_NAME.values();
    }

    public static void reload() {
        TITLES_BY_NAME.clear();
        ensureLoaded();
    }

    public static void setClientTitleModels(Collection<TitleModel> models) {
        clientTitleModels = TitleModel.sanitizedList(models);
        reload();
    }

    public static void clearClientTitleModels() {
        if (clientTitleModels != null) {
            clientTitleModels = null;
            reload();
        }
    }

    public static Title getTitle(String titleName) {
        if (titleName == null || titleName.isBlank()) {
            return null;
        }

        ensureLoaded();
        return TITLES_BY_NAME.get(path(titleName));
    }

    public static Collection<TitleModel> defaultModels() {
        return List.of(
                new TitleModel(),
                new TitleModel("fighter", List.of("aptitude/Strength/greater_or_equal/16"), false),
                new TitleModel("fighter_great", List.of("aptitude/Strength/greater_or_equal/32"), false),
                new TitleModel("warrior", List.of("aptitude/Constitution/greater_or_equal/16"), false),
                new TitleModel("warrior_great", List.of("aptitude/Constitution/greater_or_equal/32"), false),
                new TitleModel("ranger", List.of("aptitude/Dexterity/greater_or_equal/16"), false),
                new TitleModel("ranger_great", List.of("aptitude/Dexterity/greater_or_equal/32"), false),
                new TitleModel("tank", List.of("aptitude/Defense/greater_or_equal/16"), false),
                new TitleModel("tank_great", List.of("aptitude/Defense/greater_or_equal/32"), false),
                new TitleModel("alchemist", List.of("aptitude/Intelligence/greater_or_equal/16"), false),
                new TitleModel("alchemist_great", List.of("aptitude/Intelligence/greater_or_equal/32"), false),
                new TitleModel("miner", List.of("aptitude/Building/greater_or_equal/16"), false),
                new TitleModel("miner_great", List.of("aptitude/Building/greater_or_equal/32"), false),
                new TitleModel("magician", List.of("aptitude/Magic/greater_or_equal/16"), false),
                new TitleModel("magician_great", List.of("aptitude/Magic/greater_or_equal/32"), false),
                new TitleModel("lucky_one", List.of("aptitude/Luck/greater_or_equal/16"), false),
                new TitleModel("lucky_one_great", List.of("aptitude/Luck/greater_or_equal/32"), false),
                new TitleModel("dragon_slayer", List.of("EntityKilled/ender_dragon/greater_or_equal/10"), false),
                new TitleModel("player_killer", List.of("EntityKilled/player/greater_or_equal/100"), false),
                new TitleModel("mob_killer", List.of("stat/mob_kills/greater_or_equal/100"), false),
                new TitleModel("mob_killer_great", List.of("stat/mob_kills/greater_or_equal/1000"), false),
                new TitleModel("mob_killer_master", List.of("stat/mob_kills/greater_or_equal/10000"), false),
                new TitleModel("hero", List.of("stat/raid_win/greater_or_equal/100"), false),
                new TitleModel("villain", List.of("EntityKilled/villager/greater_or_equal/100"), false),
                new TitleModel("fisherman", List.of("stat/fish_caught/greater_or_equal/100"), false),
                new TitleModel("fisherman_great", List.of("stat/fish_caught/greater_or_equal/1000"), false),
                new TitleModel("fisherman_master", List.of("stat/fish_caught/greater_or_equal/10000"), false),
                new TitleModel("enchanter", List.of("stat/enchant_item/greater_or_equal/10"), false),
                new TitleModel("enchanter_great", List.of("stat/enchant_item/greater_or_equal/100"), false),
                new TitleModel("enchanter_master", List.of("stat/enchant_item/greater_or_equal/1000"), false),
                new TitleModel("survivor", List.of("stat/time_since_death/greater_or_equal/2400000"), false),
                new TitleModel("businessman", List.of("stat/traded_with_villager/greater_or_equal/100"), false),
                new TitleModel("driver_boat", List.of("stat/boat_one_cm/greater_or_equal/1000000"), false),
                new TitleModel("driver_cart", List.of("stat/minecart_one_cm/greater_or_equal/1000000"), false),
                new TitleModel("rider_horse", List.of("stat/horse_one_cm/greater_or_equal/1000000"), false),
                new TitleModel("rider_pig", List.of("stat/pig_one_cm/greater_or_equal/1000000"), false),
                new TitleModel("rider_strider", List.of("stat/strider_one_cm/greater_or_equal/1000000"), false),
                new TitleModel("traveler_nether", List.of("special/dimension/equals/minecraft:the_nether"), false, true),
                new TitleModel("traveler_end", List.of("special/dimension/equals/minecraft:the_end"), false, true));
    }

    public static Collection<ResourceLocation> defaultIds() {
        ensureLoaded();
        return TITLES_BY_NAME.values().stream().map(Title::getId).toList();
    }

    public static boolean containsDefaultId(ResourceLocation titleId) {
        ensureLoaded();
        return Optional.ofNullable(TITLES_BY_NAME.get(titleId.getPath()))
                .map(title -> title.getId().equals(titleId))
                .orElse(false);
    }

    private static Title register(String name, boolean requirement) {
        ResourceLocation key = new ResourceLocation(Constants.MOD_ID, name);
        return new Title(key, requirement, true);
    }

    private static void ensureLoaded() {
        if (!TITLES_BY_NAME.isEmpty()) {
            return;
        }

        put(TITLELESS);
        put(ADMIN);
        Collection<TitleModel> models = clientTitleModels == null ? TitleConfigService.titleModels() : clientTitleModels;
        models.forEach(model -> put(model.createTitle()));
    }

    private static void put(Title title) {
        TITLES_BY_NAME.put(title.getName(), title);
    }

    private static String path(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0) {
            return normalized.substring(namespaceSeparator + 1);
        }
        return normalized;
    }
}
