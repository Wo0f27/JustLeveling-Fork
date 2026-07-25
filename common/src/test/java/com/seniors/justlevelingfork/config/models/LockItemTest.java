package com.seniors.justlevelingfork.config.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class LockItemTest {
    @Test
    void malformedItemIdDoesNotDiscardValidEntries() {
        LockItem valid = new LockItem("minecraft:stick", new LockItem.Aptitude("Strength", 2));
        LockItem malformed = new LockItem("minecraft:nether_wart:", new LockItem.Aptitude("Magic", 8));

        List<LockItem> sanitized = LockItem.sanitizedList(List.of(valid, malformed));

        assertEquals(1, sanitized.size());
        assertEquals("minecraft:stick", sanitized.get(0).Item);
    }
}
