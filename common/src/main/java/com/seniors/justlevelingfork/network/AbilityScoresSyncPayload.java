package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.player.AbilityScoreBonusService;
import com.seniors.justlevelingfork.common.player.AbilityScoreClientState;
import com.seniors.justlevelingfork.common.player.PlayerProgress;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record AbilityScoresSyncPayload(
        Map<String, Integer> scores) {

    private static final int MAX_ABILITIES = 16;
    private static final int MAX_NAME_LENGTH = 32;

    public AbilityScoresSyncPayload {

        scores =
                Map.copyOf(
                        scores == null
                                ? Map.of()
                                : scores);
    }

    public static AbilityScoresSyncPayload current(
            ServerPlayer player,
            PlayerProgress progress) {

        Map<String, Integer> scores =
                new LinkedHashMap<>();

        scores.put(
                "strength",
                AbilityScoreBonusService.getAbilityScore(
                        player,
                        progress,
                        RegistryAptitudes.STRENGTH));

        scores.put(
                "dexterity",
                AbilityScoreBonusService.getAbilityScore(
                        player,
                        progress,
                        RegistryAptitudes.DEXTERITY));

        scores.put(
                "constitution",
                AbilityScoreBonusService.getAbilityScore(
                        player,
                        progress,
                        RegistryAptitudes.CONSTITUTION));

        scores.put(
                "intelligence",
                AbilityScoreBonusService.getAbilityScore(
                        player,
                        progress,
                        RegistryAptitudes.INTELLIGENCE));

        scores.put(
                "wisdom",
                AbilityScoreBonusService.getAbilityScore(
                        player,
                        progress,
                        RegistryAptitudes.WISDOM));

        scores.put(
                "charisma",
                AbilityScoreBonusService.getAbilityScore(
                        player,
                        progress,
                        RegistryAptitudes.CHARISMA));

        return new AbilityScoresSyncPayload(
                scores);
    }

    public void write(
            FriendlyByteBuf buffer) {

        buffer.writeVarInt(
                scores.size());

        scores.forEach((name, score) -> {

            buffer.writeUtf(
                    name,
                    MAX_NAME_LENGTH);

            buffer.writeInt(
                    score);
        });
    }

    public static AbilityScoresSyncPayload read(
            FriendlyByteBuf buffer) {

        int count =
                buffer.readVarInt();

        if (count < 0
                || count > MAX_ABILITIES) {

            throw new IllegalArgumentException(
                    "Invalid ability score count: "
                            + count);
        }

        Map<String, Integer> scores =
                new LinkedHashMap<>();

        for (int i = 0; i < count; i++) {

            String name =
                    buffer.readUtf(
                            MAX_NAME_LENGTH);

            int score =
                    buffer.readInt();

            scores.put(
                    name,
                    score);
        }

        return new AbilityScoresSyncPayload(
                scores);
    }

    public void apply() {

        AbilityScoreClientState.replace(
                scores);
    }
}