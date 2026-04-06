package chorus.simpletrackingcompass;

import chorus.simpletrackingcompass.network.ServerNetworking;
import chorus.simpletrackingcompass.network.packet.Ping;
import chorus.simpletrackingcompass.network.packet.Pong;
import chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SimpleTrackingCompass implements ModInitializer {
    public static final String MOD_ID = "simpletrackingcompass";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(PlayerPositionRequest.ID, PlayerPositionRequest.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerPositionResponse.ID, PlayerPositionResponse.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(Ping.ID, Ping.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Pong.ID, Pong.CODEC);

        ServerNetworking.init();
    }
}
