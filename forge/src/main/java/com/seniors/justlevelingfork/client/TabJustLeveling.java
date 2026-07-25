package com.seniors.justlevelingfork.client;

import com.seniors.justlevelingfork.client.screen.AptitudesOverviewScreen;
import dev.xkmc.l2tabs.tabs.core.BaseTab;
import dev.xkmc.l2tabs.tabs.core.TabManager;
import dev.xkmc.l2tabs.tabs.core.TabToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public class TabJustLeveling extends BaseTab<TabJustLeveling> {
    public TabJustLeveling(TabToken<TabJustLeveling> token, TabManager manager, ItemStack stack, Component title) {
        super(token, manager, stack, title);
    }

    @Override
    public void onTabClicked() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        Minecraft.getInstance().setScreen(new AptitudesOverviewScreen());
    }
}
