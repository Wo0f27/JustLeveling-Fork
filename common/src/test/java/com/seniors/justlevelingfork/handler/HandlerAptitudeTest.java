package com.seniors.justlevelingfork.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HandlerAptitudeTest {
    @Test
    void unlockedItemsAreNeverDropped() {
        assertFalse(HandlerAptitude.shouldDropLockedItem(false, false, true, true));
    }

    @Test
    void lockedItemsStayWhenInventoryStorageIsAllowed() {
        assertFalse(HandlerAptitude.shouldDropLockedItem(true, true, true, true));
    }

    @Test
    void globalDropSettingDropsAnyLockedItem() {
        assertTrue(HandlerAptitude.shouldDropLockedItem(true, false, true, false));
    }

    @Test
    void perItemMarkerDropsLockedItem() {
        assertTrue(HandlerAptitude.shouldDropLockedItem(true, false, false, true));
    }

    @Test
    void ordinaryLockedItemCanRemainWithDefaultDropSettings() {
        assertFalse(HandlerAptitude.shouldDropLockedItem(true, false, false, false));
    }
}
