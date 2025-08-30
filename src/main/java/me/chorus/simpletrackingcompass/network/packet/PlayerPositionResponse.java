package me.chorus.simpletrackingcompass.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static me.chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public record PlayerPositionResponse(Vec3d pos, Identifier dimensionId) implements CustomPayload {
    public static final Id<PlayerPositionResponse> ID = new Id<>(Identifier.of(MOD_ID, "response_position"));

    public static final PacketCodec<PacketByteBuf, PlayerPositionResponse> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeVec3d(payload.pos);
                buf.writeIdentifier(payload.dimensionId);
            },
            buf -> new PlayerPositionResponse(
                    buf.readVec3d(),
                    buf.readIdentifier()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}