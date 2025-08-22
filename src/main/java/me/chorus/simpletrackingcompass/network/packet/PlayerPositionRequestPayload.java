package me.chorus.simpletrackingcompass.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

import static me.chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public record PlayerPositionRequestPayload(UUID targetUuid) implements CustomPayload {
    public static final Id<PlayerPositionRequestPayload> ID = new Id<>(Identifier.of(MOD_ID, "request_position"));

    public static final PacketCodec<PacketByteBuf, PlayerPositionRequestPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeUuid(payload.targetUuid),
            buf -> new PlayerPositionRequestPayload(buf.readUuid())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}