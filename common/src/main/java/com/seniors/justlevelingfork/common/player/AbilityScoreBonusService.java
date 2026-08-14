package com.seniors.justlevelingfork.common.player;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class AbilityScoreBonusService {

    @FunctionalInterface
    public interface Provider {
        int getBonus(ServerPlayer player, Aptitude aptitude);
    }

    private static final Map<ResourceLocation, Provider> PROVIDERS =
            new ConcurrentHashMap<>();

    private AbilityScoreBonusService() {
    }

    public static boolean registerProvider(
            ResourceLocation id,
            Provider provider) {

        if (id == null || provider == null) {
            return false;
        }

        PROVIDERS.put(id, provider);
        return true;
    }

    public static void unregisterProvider(ResourceLocation id) {
        if (id != null) {
            PROVIDERS.remove(id);
        }
    }

    public static int getExternalBonus(
            ServerPlayer player,
            Aptitude aptitude) {

        if (player == null || aptitude == null) {
            return 0;
        }

        int total = 0;

        for (Map.Entry<ResourceLocation, Provider> entry : PROVIDERS.entrySet()) {
            try {
                total += entry.getValue().getBonus(player, aptitude);
            } catch (RuntimeException exception) {
                Constants.LOG.warn(
                        "Ability score bonus provider {} failed.",
                        entry.getKey(),
                        exception);
            }
        }

        return total;
    }

    public static int getAbilityScore(
            ServerPlayer player,
            PlayerProgress progress,
            Aptitude aptitude) {

        if (progress == null || aptitude == null) {
            return 10;
        }

        return AbilityScoreService.abilityScore(
                progress.getAptitudeLevel(aptitude),
                getExternalBonus(player, aptitude));
    }

    public static int getAbilityModifier(
            ServerPlayer player,
            PlayerProgress progress,
            Aptitude aptitude) {

        if (progress == null || aptitude == null) {
            return 0;
        }

        return AbilityScoreService.abilityModifier(
                progress.getAptitudeLevel(aptitude),
                getExternalBonus(player, aptitude));
    }
}