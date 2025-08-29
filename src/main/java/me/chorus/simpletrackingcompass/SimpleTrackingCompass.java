package me.chorus.simpletrackingcompass;

import me.chorus.simpletrackingcompass.network.ServerNetworking;
import me.chorus.simpletrackingcompass.network.packet.ModPingPayload;
import me.chorus.simpletrackingcompass.network.packet.ModPongPayload;
import me.chorus.simpletrackingcompass.network.packet.PlayerPositionRequestPayload;
import me.chorus.simpletrackingcompass.network.packet.PlayerPositionResponsePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SimpleTrackingCompass implements ModInitializer {
    public static final String MOD_ID = "simpletrackingcompass";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(PlayerPositionRequestPayload.ID, PlayerPositionRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerPositionResponsePayload.ID, PlayerPositionResponsePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(ModPingPayload.ID, ModPingPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPongPayload.ID, ModPongPayload.CODEC);

        ServerNetworking.init();
    }
}
