package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.Constants;
import net.minecraft.resources.ResourceLocation;

public final class PlayerProgressNetwork {
    public static final ResourceLocation CHANNEL = id("network");
    public static final ResourceLocation APTITUDE_LEVEL_UP = id("aptitude_level_up");
    public static final ResourceLocation PASSIVE_LEVEL_UP = id("passive_level_up");
    public static final ResourceLocation PASSIVE_LEVEL_DOWN = id("passive_level_down");
    public static final ResourceLocation SET_PLAYER_TITLE = id("set_player_title");
    public static final ResourceLocation TOGGLE_SKILL = id("toggle_skill");
    public static final ResourceLocation OPEN_ENDER_CHEST = id("open_ender_chest");
    public static final ResourceLocation SYNC = id("player_progress_sync");
    public static final ResourceLocation SKILL_MESSAGE = id("skill_message");
    public static final ResourceLocation APTITUDE_WARNING = id("aptitude_warning");
    public static final ResourceLocation TITLE_UNLOCK = id("title_unlock");
    public static final ResourceLocation LOCK_ITEM_SYNC = id("lock_item_sync");
    public static final ResourceLocation COMMON_CONFIG_SYNC = id("common_config_sync");

    private PlayerProgressNetwork() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }
}
