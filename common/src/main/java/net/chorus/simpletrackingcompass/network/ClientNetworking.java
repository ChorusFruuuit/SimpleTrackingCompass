package net.chorus.simpletrackingcompass.network;

import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;
import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public final class ClientNetworking {
    private static Predicate<Identifier> channelPresenceTest =
        channelId -> false; // Default implementation. To be overridden by mod loader.

    public static void onPlayerPositionResponseReceive(PlayerPositionResponse payload) {
        if (payload == null) return;

        PacketDestination dest = DestinationStorage.get(payload.payloadID());
        if (dest != null) dest.processPacket(payload);
    }

    public static void sendServerboundPacket(CustomPacketPayload payload) {
        if (payload == null) {
            LOGGER.error("Payload cannot be null");
            return;
        }

        if (client().getConnection() == null) {
            LOGGER.warn("Cannot send serverbound packets when not in game!");
            return;
        }

        if (!canSend(payload.type().id())) {
            LOGGER.warn(
                "Skipping send of {} - channel not negotiated on this connection (server may not have the mod)",
                payload.type().id()
            );
            return;
        }

        LOGGER.debug("Verification complete. Sending {} to server.", payload.type().id());
        client().getConnection().send(new ServerboundCustomPayloadPacket(payload));
    }

    public static void setChannelPresenceTest(Predicate<Identifier> channelPresenceTest) {
        ClientNetworking.channelPresenceTest = channelPresenceTest;
    }

    public static boolean canSend(Identifier channelName) {
        return channelPresenceTest.test(channelName);
    }
}
