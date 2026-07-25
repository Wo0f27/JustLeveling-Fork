package com.seniors.justlevelingfork.common.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.seniors.justlevelingfork.config.models.LockItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LockItemRegistrationTest {
    private static final String STICK = "minecraft:stick";

    @Test
    void removingUnknownAptitudeDoesNotDeleteLastRequirement() {
        List<LockItem> lockItems = new ArrayList<>();
        lockItems.add(new LockItem("minecraft:stick", new LockItem.Aptitude("Strength", 5)));

        LockItemRegistration.Result result =
                LockItemRegistration.apply(lockItems, STICK, "Dexterity", 0);

        assertEquals(LockItemRegistration.Result.NO_MATCHING_APTITUDE, result);
        assertEquals(1, lockItems.size());
        assertEquals("Strength", lockItems.get(0).Aptitudes.get(0).Aptitude.toString());
    }

    @Test
    void removingLastMatchingAptitudeDeletesItem() {
        List<LockItem> lockItems = new ArrayList<>();
        lockItems.add(new LockItem("minecraft:stick", new LockItem.Aptitude("Strength", 5)));

        LockItemRegistration.Result result =
                LockItemRegistration.apply(lockItems, STICK, "Strength", 0);

        assertEquals(LockItemRegistration.Result.REMOVED_ITEM, result);
        assertEquals(List.of(), lockItems);
    }

    @Test
    void addingAndUpdatingRequirementsUsesMutableConfigData() {
        List<LockItem> lockItems = new ArrayList<>();

        assertEquals(
                LockItemRegistration.Result.ADDED_ITEM,
                LockItemRegistration.apply(lockItems, STICK, "Strength", 5));
        assertEquals(
                LockItemRegistration.Result.UPDATED_ITEM,
                LockItemRegistration.apply(lockItems, STICK, "Strength", 8));

        assertEquals(1, lockItems.size());
        assertEquals(1, lockItems.get(0).Aptitudes.size());
        assertEquals(8, lockItems.get(0).Aptitudes.get(0).Level);
    }
}
