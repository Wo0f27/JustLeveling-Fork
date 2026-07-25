package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.client.core.Aptitudes;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.common.player.PlayerProgressClientState;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixItemStack {
    @Inject(method = "appendEnchantmentNames", at = @At("HEAD"), cancellable = true)
    private static void justlevelingfork$appendEnchantmentNames(
            List<Component> list, ListTag tags, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        boolean canReadEnchantments = PlayerProgressClientState.isSkillEnabled(RegistrySkills.SCHOLAR);

        for (int i = 0; i < tags.size(); i++) {
            CompoundTag tag = tags.getCompound(i);
            BuiltInRegistries.ENCHANTMENT
                    .getOptional(EnchantmentHelper.getEnchantmentId(tag))
                    .ifPresent(enchantment -> list.add(canReadEnchantments
                            ? enchantment.getFullname(EnchantmentHelper.getEnchantmentLevel(tag))
                            : Component.translatable("tooltip.skill.scholar.lock_item").withStyle(ChatFormatting.GRAY)));
        }
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void justlevelingfork$appendUsageRequirements(
            Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> callbackInfo) {
        ItemStack stack = (ItemStack) (Object) this;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Aptitudes> requirements = itemId == null ? null : HandlerAptitude.getValue(itemId.toString());
        if (requirements == null || requirements.isEmpty()) {
            return;
        }

        PlayerProgress progress = PlayerProgressClientState.get().orElse(null);
        if (progress == null) {
            return;
        }

        List<Component> tooltip = callbackInfo.getReturnValue();
        boolean addedHeader = false;
        for (Aptitudes requirement : requirements) {
            int playerLevel = progress.getAptitudeLevel(requirement.getKey());
            boolean met = playerLevel >= requirement.getAptitudeLvl();
            if (met && CommonConfigService.hideMetUsageRequirements()) {
                continue;
            }

            if (!addedHeader) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("tooltip.aptitude.item_requirement").withStyle(ChatFormatting.DARK_PURPLE));
                addedHeader = true;
            }

            tooltip.add(Component.translatable(
                            "tooltip.aptitude.item_requirements",
                            Component.translatable(requirement.getAptitude().getKey()),
                            Component.literal(String.valueOf(requirement.getAptitudeLvl())))
                    .withStyle(met ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
        }
    }
}
