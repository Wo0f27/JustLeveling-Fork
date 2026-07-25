package com.seniors.justlevelingfork.mixin;

import com.seniors.justlevelingfork.common.event.MiningSkillEffects;
import com.seniors.justlevelingfork.common.event.CombatSkillEffects;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void justlevelingfork$getDestroySpeed(BlockState state, CallbackInfoReturnable<Float> callbackInfo) {
        callbackInfo.setReturnValue(MiningSkillEffects.adjustDestroySpeed(
                (Player) (Object) this, state, callbackInfo.getReturnValue()));
    }

    @ModifyVariable(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            ordinal = 0)
    private float justlevelingfork$adjustAttackDamage(float damage, Entity target) {
        return CombatSkillEffects.adjustPlayerAttackDamage((Player) (Object) this, target, damage);
    }
}
