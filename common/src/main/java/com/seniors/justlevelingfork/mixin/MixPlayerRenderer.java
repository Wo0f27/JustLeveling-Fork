package com.seniors.justlevelingfork.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public MixPlayerRenderer(EntityRendererProvider.Context context, boolean slim) {
        super(context, new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.5F);
    }

    @Inject(
            method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void justlevelingfork$renderTitleNameTag(
            AbstractClientPlayer entity,
            Component nameTag,
            PoseStack matrices,
            MultiBufferSource buffer,
            int light,
            CallbackInfo callbackInfo) {
        Component customName = entity.getCustomName();
        if (customName == null || customName.equals(Component.translatable(RegistryTitles.TITLELESS.getKey()))) {
            return;
        }

        MutableComponent title = Component.literal("<")
                .append(customName.copy().withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD))
                .append(Component.literal(">"));
        this.justlevelingfork$drawTitle(title, 10, entity, matrices, buffer, light);
    }

    @Unique
    private void justlevelingfork$drawTitle(
            Component component, int y, Entity entity, PoseStack matrices, MultiBufferSource buffer, int light) {
        boolean visibleThroughWalls = !entity.isDiscrete();
        float nameTagOffset = entity.getNameTagOffsetY();
        matrices.pushPose();
        matrices.translate(0.0F, nameTagOffset, 0.0F);
        matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = matrices.last().pose();
        float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;
        Font font = getFont();
        float x = -font.width(component) / 2.0F;
        font.drawInBatch(
                component,
                x,
                -y,
                553648127,
                false,
                matrix,
                buffer,
                visibleThroughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                backgroundColor,
                light);
        if (visibleThroughWalls) {
            font.drawInBatch(component, x, -y, -1, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, light);
        }
        matrices.popPose();
    }
}
