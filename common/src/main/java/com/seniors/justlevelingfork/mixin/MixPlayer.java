package com.seniors.justlevelingfork.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.seniors.justlevelingfork.common.event.CombatSkillEffects;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class MixPlayer extends LivingEntity {
    protected MixPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public int getMaxAirSupply() {
        if ((Object) this instanceof ServerPlayer player
                && player.server != null
                && PlayerProgressService.isSkillEnabled(player, RegistrySkills.ATHLETICS)) {
            return (int) (300.0D * RegistrySkills.ATHLETICS.getValue()[0]);
        }
        return 300;
    }

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean justlevelingfork$adjustAttackDamage(
            Entity target, DamageSource source, float damage, Operation<Boolean> original) {
        float adjustedDamage = CombatSkillEffects.adjustPlayerAttackDamage(
                (Player) (Object) this, target, damage);
        return original.call(target, source, adjustedDamage);
    }
}
