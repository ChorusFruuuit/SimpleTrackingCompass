package net.chorus.simpletrackingcompass;

import net.chorus.simpletrackingcompass.network.ClientNetworking;
import net.chorus.simpletrackingcompass.network.ServerNetworking;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(SimpleTrackingCompass.MOD_ID)
public class SimpleTrackingCompassNeoForge {
    private static String version;

    public SimpleTrackingCompassNeoForge(IEventBus eventBus, ModContainer container, Dist dist) {
        version = container.getModInfo().getVersion().toString();

        // network registration
        eventBus.addListener(this::registerPayloads);
        ServerNetworking.setChannelPresenceTest((packetListener, channelId) ->
            packetListener != null && packetListener.hasChannel(channelId)
        );

        if (!dist.isClient()) return;

        SimpleTrackingCompassNeoForgeClient.init(eventBus);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(version).optional();

        registrar.playToServer(
            PlayerPositionRequest.TYPE,
            PlayerPositionRequest.CODEC,
            (payload, context) -> {
                if (context.player() instanceof ServerPlayer serverPlayer)
                    ServerNetworking.onPlayerPositionRequestReceive(payload, serverPlayer);
            }
        );
        registrar.playToClient(
            PlayerPositionResponse.TYPE,
            PlayerPositionResponse.CODEC,
            (payload, context) ->
                ClientNetworking.onPlayerPositionResponseReceive(payload)
        );
    }
}
