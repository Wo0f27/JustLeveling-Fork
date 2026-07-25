package com.seniors.justlevelingfork.network;

import com.seniors.justlevelingfork.common.player.PlayerProgressActions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricServerNetworking {
    private FabricServerNetworking() {
    }

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerProgressNetwork.APTITUDE_LEVEL_UP, (server, player, handler, buffer, responseSender) -> {
                    String aptitudeName = buffer.readUtf();
                    server.execute(() -> PlayerProgressActions.levelUpAptitude(player, aptitudeName));
                });
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerProgressNetwork.PASSIVE_LEVEL_UP, (server, player, handler, buffer, responseSender) -> {
                    String passiveName = buffer.readUtf();
                    server.execute(() -> PlayerProgressActions.levelUpPassive(player, passiveName));
                });
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerProgressNetwork.PASSIVE_LEVEL_DOWN, (server, player, handler, buffer, responseSender) -> {
                    String passiveName = buffer.readUtf();
                    server.execute(() -> PlayerProgressActions.levelDownPassive(player, passiveName));
                });
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerProgressNetwork.SET_PLAYER_TITLE, (server, player, handler, buffer, responseSender) -> {
                    String titleName = buffer.readUtf();
                    server.execute(() -> PlayerProgressActions.setPlayerTitle(player, titleName));
                });
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerProgressNetwork.TOGGLE_SKILL, (server, player, handler, buffer, responseSender) -> {
                    String skillName = buffer.readUtf();
                    boolean enabled = buffer.readBoolean();
                    server.execute(() -> PlayerProgressActions.setToggleSkill(player, skillName, enabled));
                });
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerProgressNetwork.OPEN_ENDER_CHEST, (server, player, handler, buffer, responseSender) ->
                        server.execute(() -> PlayerProgressActions.openEnderChest(player)));
    }
}
