package com.seniors.justlevelingfork.mixin;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.client.AttackRangeExtensions;
import net.bettercombat.client.collision.OrientedBoundingBox;
import net.bettercombat.client.collision.TargetFinder;
import net.bettercombat.client.collision.WeaponHitBoxes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.bettercombat.client.collision.TargetFinder", remap = false)
public abstract class MixBetterCombatTargetFinder {
    @Unique
    private static final UUID JUSTLEVELINGFORK_REACH_MODIFIER_ID =
            UUID.fromString("96a891fe-5919-418d-8205-f50464391509");

    @Inject(method = "findAttackTargetResult", at = @At("HEAD"), cancellable = true, remap = false)
    private static void justlevelingfork$findAttackTargetResult(
            Player player,
            Entity cursorTarget,
            WeaponAttributes.Attack attack,
            double attackRange,
            CallbackInfoReturnable<TargetFinder.TargetResult> callbackInfo) {
        if (player == null || cursorTarget == null || !ForgeMod.ENTITY_REACH.isPresent()) {
            return;
        }

        AttributeInstance playerReach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
        if (playerReach == null) {
            return;
        }

        AttributeModifier modifier = playerReach.getModifier(JUSTLEVELINGFORK_REACH_MODIFIER_ID);
        if (modifier == null) {
            return;
        }

        callbackInfo.cancel();
        Vec3 origin = TargetFinder.getInitialTracingPoint(player);
        List<Entity> entities = TargetFinder.getInitialTargets(player, cursorTarget, attackRange);
        if (!AttackRangeExtensions.sources().isEmpty()) {
            attackRange = justlevelingfork$applyAttackRangeModifiers(player, attackRange);
        }

        attackRange += modifier.getAmount();
        if (ModList.get().isLoaded("quality_equipment")) {
            attackRange += 3.0D;
        }

        boolean spinAttack = attack.angle() > 180.0D;
        Vec3 size = WeaponHitBoxes.createHitbox(attack.hitbox(), attackRange, spinAttack);
        OrientedBoundingBox obb = new OrientedBoundingBox(origin, size, player.getXRot(), player.getYRot());
        if (!spinAttack) {
            obb = obb.offsetAlongAxisZ(size.z / 2.0D);
        }

        obb.updateVertex();
        TargetFinder.CollisionFilter collisionFilter = new TargetFinder.CollisionFilter(obb);
        entities = collisionFilter.filter(entities);
        TargetFinder.RadialFilter radialFilter = new TargetFinder.RadialFilter(origin, obb.axisZ, attackRange, attack.angle());
        entities = radialFilter.filter(entities);
        callbackInfo.setReturnValue(new TargetFinder.TargetResult(entities, obb));
    }

    @Unique
    private static double justlevelingfork$applyAttackRangeModifiers(Player player, double attackRange) {
        AttackRangeExtensions.Context context = new AttackRangeExtensions.Context(player, attackRange);
        List<AttackRangeExtensions.Modifier> modifiers = AttackRangeExtensions.sources().stream()
                .map(function -> function.apply(context))
                .sorted(Comparator.comparingInt(AttackRangeExtensions.Modifier::operationOrder))
                .toList();
        double result = attackRange;

        for (AttackRangeExtensions.Modifier modifier : modifiers) {
            switch (modifier.operation()) {
                case ADD -> result += modifier.value();
                case MULTIPLY -> result *= modifier.value();
            }
        }
        return result;
    }
}
