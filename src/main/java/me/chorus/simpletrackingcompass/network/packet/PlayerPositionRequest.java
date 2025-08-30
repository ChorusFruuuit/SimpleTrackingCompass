package me.chorus.simpletrackingcompass.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

import static me.chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public record PlayerPositionRequest(UUID targetUuid) implements CustomPayload {
    public static final Id<PlayerPositionRequest> ID = new Id<>(Identifier.of(MOD_ID, "request_position"));

    public static final PacketCodec<PacketByteBuf, PlayerPositionRequest> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeUuid(payload.targetUuid),
            buf -> new PlayerPositionRequest(buf.readUuid())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}