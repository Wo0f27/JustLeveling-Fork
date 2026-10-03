package com.seniors.justlevelingfork.integration.crit;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.entity.Entity;

/** The target of the current vanilla melee/arrow hit, for Critical Strike's target-free crit API. */
public final class CritHitContext {
    private static final ThreadLocal<Deque<Entity>> TARGETS = ThreadLocal.withInitial(ArrayDeque::new);

    private CritHitContext() {
    }

    public static void push(Entity target) {
        TARGETS.get().push(target);
    }

    public static void pop() {
        Deque<Entity> targets = TARGETS.get();
        targets.pop();
        if (targets.isEmpty()) {
            TARGETS.remove();
        }
    }

    public static Entity target() {
        Deque<Entity> targets = TARGETS.get();
        Entity target = targets.peek();
        if (target == null) {
            TARGETS.remove();
        }
        return target;
    }
}
