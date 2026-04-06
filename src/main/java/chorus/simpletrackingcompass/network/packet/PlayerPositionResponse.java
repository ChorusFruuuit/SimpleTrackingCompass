package chorus.simpletrackingcompass.network.packet;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public record PlayerPositionResponse(Vec3 pos, Identifier dimensionId) implements CustomPacketPayload {
    public static final Type<PlayerPositionResponse> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "response_position"));

    public static final StreamCodec<FriendlyByteBuf, PlayerPositionResponse> CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC, PlayerPositionResponse::pos,
        Identifier.STREAM_CODEC, PlayerPositionResponse::dimensionId,
        PlayerPositionResponse::new
    );

    @Override
    @NonNull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}