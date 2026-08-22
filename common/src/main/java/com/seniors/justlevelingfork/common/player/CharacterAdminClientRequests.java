package com.seniors.justlevelingfork.common.player;

import java.util.Optional;
import java.util.function.Consumer;


public final class CharacterAdminClientRequests {

    private static Runnable accessRefreshSender =
            () -> {};

    private static Consumer<CharacterAdminRequest>
            actionSender =
            ignored -> {};

    public static void setActionSender(
            Consumer<CharacterAdminRequest> sender) {

        actionSender =
                Optional.ofNullable(sender)
                        .orElse(ignored -> {});
    }

    public static void requestResetCharacter() {

        actionSender.accept(
                CharacterAdminRequest
                        .resetCharacter());
    }

    public static void requestSetStartingClass(
            net.minecraft.resources.ResourceLocation classId) {

        if (classId == null) {
            return;
        }

        actionSender.accept(
                CharacterAdminRequest
                        .setStartingClass(classId));
    }

    public static void requestRecommendedAbilities() {

        actionSender.accept(
                CharacterAdminRequest
                        .applyRecommendedAbilities());
    }

    public static void requestAddXp(
            long amount) {

        if (amount <= 0L) {
            return;
        }

        actionSender.accept(
                CharacterAdminRequest
                        .addXp(amount));
    }

    public static void requestXpToNextLevel() {

        actionSender.accept(
                CharacterAdminRequest
                        .xpToNextLevel());
    }

    private CharacterAdminClientRequests() {
    }

    public static void setAccessRefreshSender(
            Runnable sender) {

        accessRefreshSender =
                Optional.ofNullable(sender)
                        .orElse(() -> {});
    }

    public static void requestAccessRefresh() {
        accessRefreshSender.run();
    }
}