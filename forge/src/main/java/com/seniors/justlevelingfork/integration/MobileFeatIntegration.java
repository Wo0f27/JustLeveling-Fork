package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Server-owned Soul Speed floor; local movement hooks mirror synced feat ownership. */
public final class MobileFeatIntegration {

    public static final ResourceLocation MOBILE =
            new ResourceLocation(Constants.MOD_ID, "mobile");

    private static final UUID SOUL_SPEED_ID =
            UUID.fromString("d5bf2b49-1231-4cf8-9e6f-c354a0de8a25");
    private static final float SOUL_SPEED_II_AMOUNT = soulSpeedAmount(2);

    private MobileFeatIntegration() {
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(MobileFeatIntegration.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        double missingSoulSpeed = 0.0D;
        if (hasMobile(player)
                && player.getBlockStateOn().is(BlockTags.SOUL_SPEED_BLOCKS)
                && !player.isFallFlying()) {
            int realLevel = EnchantmentHelper.getEnchantmentLevel(
                    Enchantments.SOUL_SPEED, player);
            if (realLevel < 2) {
                missingSoulSpeed = SOUL_SPEED_II_AMOUNT
                        - soulSpeedAmount(realLevel);
            }
        }

        AttributeModifier current = movementSpeed.getModifier(SOUL_SPEED_ID);
        if (missingSoulSpeed <= 0.0D) {
            if (current != null) {
                movementSpeed.removeModifier(SOUL_SPEED_ID);
            }
        } else if (current == null
                || Math.abs(current.getAmount() - missingSoulSpeed) > 1.0E-6D) {
            if (current != null) {
                movementSpeed.removeModifier(SOUL_SPEED_ID);
            }
            movementSpeed.addTransientModifier(new AttributeModifier(
                    SOUL_SPEED_ID,
                    "JLF Mobile Soul Speed II floor",
                    missingSoulSpeed,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    private static float soulSpeedAmount(int level) {
        return level <= 0 ? 0.0F : 0.03F * (1.0F + level * 0.35F);
    }

    /** Only the server's owner and its synced local client may alter movement. */
    public static boolean hasMobile(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return FeatProgressionService.hasFeat(serverPlayer, MOBILE);
        }

        return player != null
                && player.level().isClientSide
                && player.isLocalPlayer()
                && PlayerProgressClientState.get()
                        .map(progress -> progress.getFeatRank(MOBILE.toString()) > 0)
                        .orElse(false);
    }
}
