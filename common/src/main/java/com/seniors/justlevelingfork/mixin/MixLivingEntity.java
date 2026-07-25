package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.event.KillSkillEffects;
import com.seniors.justlevelingfork.common.event.CombatSkillEffects;
import com.seniors.justlevelingfork.common.event.PotionSkillEffects;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistryAttributes;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixLivingEntity {
    @Unique
    private List<ItemStack> justlevelingfork$equipmentBeforeDeathLoot = List.of();

    @ModifyVariable(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), argsOnly = true)
    private float justlevelingfork$reduceMagicDamage(float damage, DamageSource source) {
        AttributeInstance magicResist = justlevelingfork$livingEntity().getAttribute(RegistryAttributes.MAGIC_RESIST);
        if (magicResist == null || !source.isIndirect() || magicResist.getValue() <= 0.0D) {
            return damage;
        }

        return (float) (damage - damage * magicResist.getValue());
    }

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance justlevelingfork$adjustAddedEffect(MobEffectInstance effect) {
        return PotionSkillEffects.adjustAddedEffect(justlevelingfork$livingEntity(), effect);
    }

    @Inject(method = "getVisibilityPercent", at = @At("TAIL"), cancellable = true)
    private void justlevelingfork$getVisibilityPercent(Entity source, CallbackInfoReturnable<Double> callbackInfo) {
        if (!(justlevelingfork$livingEntity() instanceof ServerPlayer player)
                || !PlayerProgressService.isSkillEnabled(player, RegistrySkills.STEALTH_MASTERY)) {
            return;
        }

        double[] values = RegistrySkills.STEALTH_MASTERY.getValue();
        double visibilityMultiplier = player.isShiftKeyDown() ? values[0] : values[1];
        callbackInfo.setReturnValue(callbackInfo.getReturnValue() * visibilityMultiplier / 100.0D);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void justlevelingfork$captureEquipmentBeforeDeathLoot(DamageSource source, CallbackInfo callbackInfo) {
        List<ItemStack> equipment = new ArrayList<>();
        for (ItemStack stack : justlevelingfork$livingEntity().getAllSlots()) {
            equipment.add(stack.copy());
        }
        this.justlevelingfork$equipmentBeforeDeathLoot = equipment;
    }

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void justlevelingfork$applyKillSkillEffects(DamageSource source, CallbackInfo callbackInfo) {
        KillSkillEffects.afterDeathLoot(
                justlevelingfork$livingEntity(), source, this.justlevelingfork$equipmentBeforeDeathLoot);
        this.justlevelingfork$equipmentBeforeDeathLoot = List.of();
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void justlevelingfork$afterHurt(
            DamageSource source, float damage, CallbackInfoReturnable<Boolean> callbackInfo) {
        CombatSkillEffects.afterLivingHurt(
                justlevelingfork$livingEntity(), source, callbackInfo.getReturnValue());
    }

    @Unique
    private LivingEntity justlevelingfork$livingEntity() {
        return (LivingEntity) (Object) this;
    }
}
