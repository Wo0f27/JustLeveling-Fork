package com.seniors.justlevelingfork.common.player;

public final class CharacterAdminClientState {

    private static boolean allowed;

    private CharacterAdminClientState() {
    }

    public static boolean isAllowed() {
        return allowed;
    }

    public static void setAllowed(
            boolean value) {

        allowed = value;
    }

    public static void clear() {
        allowed = false;
    }
}