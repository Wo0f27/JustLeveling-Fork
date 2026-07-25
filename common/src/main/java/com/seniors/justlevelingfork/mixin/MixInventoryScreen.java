package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.client.gui.ClientTabs;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientRequests;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class MixInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> {
    @Unique
    private static final ResourceLocation JUSTLEVELINGFORK_ENDER_CHEST_BUTTON =
            new ResourceLocation(Constants.MOD_ID, "textures/skill/ender_chest_button.png");
    @Unique
    private boolean justlevelingfork$hoveringEnderChestButton;

    public MixInventoryScreen(Player player) {
        super(player.inventoryMenu, player.getInventory(), Component.translatable("container.crafting"));
    }

    @Inject(
            method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
            at = @At("TAIL"))
    private void justlevelingfork$renderEnderChestButton(
            GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo callbackInfo) {
        ClientTabs.render(graphics, (InventoryScreen) (Object) this, mouseX, mouseY, this.imageWidth, this.imageHeight);
        this.justlevelingfork$hoveringEnderChestButton = false;
        if (!PlayerProgressClientState.isSkillEnabled(RegistrySkills.WORMHOLE_STORAGE)) {
            return;
        }

        int buttonX = this.leftPos + 127 + (justlevelingfork$recipeBook().isVisible() ? 77 : 0);
        int buttonY = this.topPos + 61;
        int textureY = 0;
        if (justlevelingfork$isMouseWithin(buttonX, buttonY, mouseX, mouseY, 20, 18)) {
            textureY = 18;
            this.justlevelingfork$hoveringEnderChestButton = true;
        }

        graphics.blit(JUSTLEVELINGFORK_ENDER_CHEST_BUTTON, buttonX, buttonY, 0.0F, textureY, 20, 18, 20, 36);
    }

    @Inject(
            method = "mouseClicked(DDI)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void justlevelingfork$openEnderChest(
            double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (ClientTabs.mouseClicked((InventoryScreen) (Object) this, mouseX, mouseY, button, this.imageWidth, this.imageHeight)) {
            callbackInfo.setReturnValue(true);
            return;
        }

        if (button != 0 || !this.justlevelingfork$hoveringEnderChestButton) {
            return;
        }

        PlayerProgressClientRequests.requestOpenEnderChest();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        callbackInfo.setReturnValue(true);
    }

    @Unique
    private static boolean justlevelingfork$isMouseWithin(
            int x, int y, int mouseX, int mouseY, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Unique
    private net.minecraft.client.gui.screens.recipebook.RecipeBookComponent justlevelingfork$recipeBook() {
        return ((RecipeUpdateListener) (Object) this).getRecipeBookComponent();
    }
}
