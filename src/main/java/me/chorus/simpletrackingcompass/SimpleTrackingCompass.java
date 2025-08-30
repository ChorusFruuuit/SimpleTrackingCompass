package me.chorus.simpletrackingcompass;

import me.chorus.simpletrackingcompass.network.ServerNetworking;
import me.chorus.simpletrackingcompass.network.packet.Ping;
import me.chorus.simpletrackingcompass.network.packet.Pong;
import me.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import me.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SimpleTrackingCompass implements ModInitializer {
    public static final String MOD_ID = "simpletrackingcompass";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(PlayerPositionRequest.ID, PlayerPositionRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerPositionResponse.ID, PlayerPositionResponse.CODEC);

        PayloadTypeRegistry.playC2S().register(Ping.ID, Ping.CODEC);
        PayloadTypeRegistry.playS2C().register(Pong.ID, Pong.CODEC);

        ServerNetworking.init();
    }
}
