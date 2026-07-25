package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class MixCraftingMenu {
    @Dynamic("Verified against Minecraft 1.20.1 Mojmap with minecraft-dev MCP.")
    @Inject(
            at = @At("TAIL"),
            method = "slotChangedCraftingGrid(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/inventory/ResultContainer;)V")
    private static void justlevelingfork$slotChangedCraftingGrid(
            AbstractContainerMenu menu,
            Level level,
            Player player,
            CraftingContainer container,
            ResultContainer resultContainer,
            CallbackInfo callbackInfo) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.isCreative()) {
            return;
        }

        ItemStack result = resultContainer.getItem(0);
        if (!HandlerAptitude.canUseItem(serverPlayer, result)) {
            resultContainer.setItem(0, ItemStack.EMPTY);
        }
    }
}
