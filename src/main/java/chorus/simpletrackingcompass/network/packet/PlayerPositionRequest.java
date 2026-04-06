package chorus.simpletrackingcompass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public record PlayerPositionRequest(UUID targetUuid) implements CustomPacketPayload {
    public static final Type<PlayerPositionRequest> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "request_position"));

    public static final StreamCodec<ByteBuf, PlayerPositionRequest> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, PlayerPositionRequest::targetUuid,
        PlayerPositionRequest::new
    );

    @Override
    @NonNull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}