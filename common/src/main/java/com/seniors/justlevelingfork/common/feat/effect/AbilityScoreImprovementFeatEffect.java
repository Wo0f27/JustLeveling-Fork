package com.seniors.justlevelingfork.common.feat.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.feat.FeatDefinition;
import com.seniors.justlevelingfork.common.feat.FeatEffectDefinition;
import com.seniors.justlevelingfork.common.player.AbilityScoreBonusService;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public final class AbilityScoreImprovementFeatEffect
        implements FeatEffect {

    public static final AbilityScoreImprovementFeatEffect INSTANCE =
            new AbilityScoreImprovementFeatEffect();

    private AbilityScoreImprovementFeatEffect() {
    }

    @Override
    public boolean canApply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {

        if (player == null
                || progress == null
                || feat == null
                || effect == null
                || choice == null
                || choice.isBlank()) {

            return false;
        }

        int points =
                getPoints(effect);

        if (points <= 0) {
            return false;
        }

        Map<Aptitude, Integer> allocation =
                parseAllocation(
                        effect,
                        choice,
                        points);

        if (allocation.isEmpty()) {
            return false;
        }

        int spent =
                allocation.values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .sum();

        if (spent != points) {
            return false;
        }

        int maximumScore =
                getMaximumScore(
                        effect);

        for (Map.Entry<Aptitude, Integer> entry
                : allocation.entrySet()) {

            Aptitude aptitude =
                    entry.getKey();

            int increase =
                    entry.getValue();

            if (increase <= 0) {
                return false;
            }

            int currentRawLevel =
                    progress.getAptitudeLevel(
                            aptitude);

            /*
             * JLF's own raw aptitude cap still applies.
             */
            if (currentRawLevel + increase
                    > CommonConfigService
                    .aptitudeMaxLevel()) {

                return false;
            }

            /*
             * Feat-based ability increases respect the
             * BG3-style score cap.
             *
             * We use the effective score so external
             * ancestry bonuses cannot accidentally allow
             * the feat to push the final score beyond
             * the configured feat maximum.
             */
            int currentEffectiveScore =
                    AbilityScoreBonusService
                            .getAbilityScore(
                                    player,
                                    progress,
                                    aptitude);

            if (currentEffectiveScore + increase
                    > maximumScore) {

                return false;
            }
        }

        return true;
    }

    @Override
    public void apply(
            ServerPlayer player,
            PlayerProgress progress,
            FeatDefinition feat,
            FeatEffectDefinition effect,
            String choice) {

        if (progress == null
                || effect == null
                || choice == null
                || choice.isBlank()) {

            return;
        }

        int points =
                getPoints(effect);

        Map<Aptitude, Integer> allocation =
                parseAllocation(
                        effect,
                        choice,
                        points);

        allocation.forEach(
                (aptitude, increase) -> {

                    int current =
                            progress.getAptitudeLevel(
                                    aptitude);

                    progress.setAptitudeLevel(
                            aptitude,
                            current + increase);
                });
    }

    private Map<Aptitude, Integer> parseAllocation(
            FeatEffectDefinition effect,
            String choice,
            int points) {

        if (points <= 0
                || choice == null
                || choice.isBlank()) {

            return Map.of();
        }

        Set<String> allowed =
                getAllowedAbilities(
                        effect);

        String normalized =
                choice.trim()
                        .toLowerCase(
                                Locale.ROOT);

        /*
         * Legacy/simple UI format:
         *
         * "strength"
         *
         * Means spend every available point
         * on that one ability.
         */
        if (!normalized.contains(",")
                && !normalized.contains(":")) {

            Aptitude aptitude =
                    RegistryAptitudes.getAptitude(
                            normalized);

            if (aptitude == null
                    || !isAllowed(
                    aptitude,
                    allowed)) {

                return Map.of();
            }

            return Map.of(
                    aptitude,
                    points);
        }

        /*
         * Explicit allocation format:
         *
         * strength:2
         *
         * or:
         *
         * strength:1,dexterity:1
         */
        Map<Aptitude, Integer> result =
                new LinkedHashMap<>();

        String[] entries =
                normalized.split(",");

        for (String entry : entries) {

            if (entry == null
                    || entry.isBlank()) {

                return Map.of();
            }

            String[] parts =
                    entry.trim()
                            .split(":");

            if (parts.length < 1
                    || parts.length > 2) {

                return Map.of();
            }

            String aptitudeName =
                    parts[0].trim();

            Aptitude aptitude =
                    RegistryAptitudes.getAptitude(
                            aptitudeName);

            if (aptitude == null
                    || !isAllowed(
                    aptitude,
                    allowed)) {

                return Map.of();
            }

            int amount;

            if (parts.length == 1) {

                amount = 1;

            } else {

                try {

                    amount =
                            Integer.parseInt(
                                    parts[1].trim());

                } catch (NumberFormatException exception) {

                    return Map.of();
                }
            }

            if (amount <= 0) {
                return Map.of();
            }

            result.merge(
                    aptitude,
                    amount,
                    Integer::sum);
        }

        return result;
    }

    private boolean isAllowed(
            Aptitude aptitude,
            Set<String> allowed) {

        if (allowed.isEmpty()) {
            return true;
        }

        return allowed.contains(
                aptitude.getName()
                        .toLowerCase(
                                Locale.ROOT));
    }

    private Set<String> getAllowedAbilities(
            FeatEffectDefinition effectDefinition) {

        JsonObject effect =
                effectDefinition.getData();

        if (!effect.has("allowed_abilities")
                || !effect.get("allowed_abilities")
                .isJsonArray()) {

            return Set.of();
        }

        JsonArray array =
                effect.getAsJsonArray(
                        "allowed_abilities");

        Set<String> result =
                new LinkedHashSet<>();

        for (JsonElement element : array) {

            if (element == null
                    || !element.isJsonPrimitive()) {

                continue;
            }

            String name =
                    element.getAsString()
                            .toLowerCase(
                                    Locale.ROOT);

            if (RegistryAptitudes
                    .getAptitude(name) != null) {

                result.add(name);
            }
        }

        return Set.copyOf(
                result);
    }

    private int getPoints(
            FeatEffectDefinition effectDefinition) {

        JsonObject effect =
                effectDefinition.getData();

        /*
         * New preferred name.
         */
        if (effect.has("points")) {

            return GsonHelper.getAsInt(
                    effect,
                    "points",
                    1);
        }

        /*
         * Backwards compatibility with our
         * original datapack schema.
         */
        return GsonHelper.getAsInt(
                effect,
                "amount",
                1);
    }

    private int getMaximumScore(
            FeatEffectDefinition effectDefinition) {

        JsonObject effect =
                effectDefinition.getData();

        return GsonHelper.getAsInt(
                effect,
                "max_score",
                20);
    }
}