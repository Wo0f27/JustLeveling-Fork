package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.common.feat.FeatProgressionService;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.registry.ForgeRegistryMobEffects;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Mobile's server-owned terrain and melee burst behavior; fluid movement mirrors synced ownership. */
public final class MobileFeatIntegration {

    public static final ResourceLocation MOBILE =
            new ResourceLocation(Constants.MOD_ID, "mobile");

    private static final UUID SOUL_SPEED_ID =
            UUID.fromString("d5bf2b49-1231-4cf8-9e6f-c354a0de8a25");
    private static final float SOUL_SPEED_II_AMOUNT = soulSpeedAmount(2);
    private static final int REPOSITION_DURATION_TICKS = 40;

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.isSpectator()
                || !isDirectMeleeHit(event.getSource(), player)
                || !isValidRepositionTarget(player, event.getEntity())
                || !FeatProgressionService.hasFeat(player, MOBILE)) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                ForgeRegistryMobEffects.MOBILE_REPOSITION.get(),
                REPOSITION_DURATION_TICKS,
                0,
                false,
                false,
                false));
    }

    private static boolean isDirectMeleeHit(DamageSource source, ServerPlayer player) {
        return source.getDirectEntity() == player
                && source.is(DamageTypes.PLAYER_ATTACK);
    }

    private static boolean isValidRepositionTarget(ServerPlayer player, LivingEntity target) {
        if (target == player
                || !target.isAlive()
                || target.isRemoved()
                || player.isAlliedTo(target)
                || target.isAlliedTo(player)) {
            return false;
        }

        return !(target instanceof Player otherPlayer)
                || (!otherPlayer.isCreative()
                && !otherPlayer.isSpectator()
                && player.canHarmPlayer(otherPlayer));
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
