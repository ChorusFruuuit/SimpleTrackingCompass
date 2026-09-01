package net.chorus.simpletrackingcompass.network;

import net.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.chorus.simpletrackingcompass.util.PlayerUtils;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;

public final class ServerNetworking {
    private static BiPredicate<ServerGamePacketListenerImpl, ResourceLocation> channelPresenceTest =
        (packetListener, channelId) -> false;// Default implementation. To be overridden by mod loader.

    @SuppressWarnings("resource")
    public static void onPlayerPositionRequestReceive(PlayerPositionRequest payload, ServerPlayer requester) {
        if (payload == null || requester == null) return;

        Optional<UUID> playerUUID = payload.playerUUID();
        MinecraftServer server = requester.level().getServer();
        ServerPlayer player = playerUUID.isPresent()
            ? (server != null ? server.getPlayerList().getPlayer(playerUUID.get()) : null)
            : PlayerUtils.getNearestPlayer(requester, server != null ? server.getPlayerList().getPlayers() : null);

        if (player != null) {
            Vec3 playerPosition = player.position();
            ResourceKey<Level> playerDimension = player.level().dimension();

            sendClientboundPacket(new PlayerPositionResponse(
                playerUUID.isPresent() ? playerUUID : Optional.of(player.getUUID()),
                Optional.of(playerPosition),
                Optional.of(playerDimension),
                payload.payloadID()
            ), requester);
        } else {
            sendClientboundPacket(new PlayerPositionResponse(
                playerUUID,
                Optional.empty(),
                Optional.empty(),
                payload.payloadID()
            ), requester);
        }
    }

    public static void sendClientboundPacket(CustomPacketPayload payload, ServerPlayer packetReceiver) {
        if (payload == null) {
            LOGGER.error("Payload cannot be null");
            return;
        }

        if (packetReceiver == null) {
            LOGGER.error("Packet Receiver cannot be null");
            return;
        }

        if (!canSend(packetReceiver.connection, payload.type().id())) {
            LOGGER.warn(
                "Skipping send of {} to {} - channel not negotiated on this connection (client may not have the mod)",
                payload.type().id(), packetReceiver.getGameProfile().getName()
            );
            return;
        }

        LOGGER.debug("Verification complete. Sending {} to {}.", payload.type().id(), packetReceiver.getGameProfile().getName());
        try {
            packetReceiver.connection.send(new ClientboundCustomPayloadPacket(payload));
        } catch (Throwable t) {
            LOGGER.warn("The send of {} to {} failed!", payload.type().id(), packetReceiver.getGameProfile().getName(), t);
        }
    }

    public static void setChannelPresenceTest(BiPredicate<ServerGamePacketListenerImpl, ResourceLocation> channelPresenceTest) {
        ServerNetworking.channelPresenceTest = channelPresenceTest;
    }

    public static boolean canSend(ServerGamePacketListenerImpl packetListener, ResourceLocation channelId) {
        return channelPresenceTest.test(packetListener, channelId);
    }
}
