package net.chorus.simpletrackingcompass.network.packet;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;

/**
 * If the {@code playerUUID} is {@code null} responds with the closest player's position
 * */
public record PlayerPositionRequest(Optional<UUID> playerUUID, ResourceLocation payloadID) implements CustomPacketPayload {
    public static final Type<PlayerPositionRequest> TYPE = new Type<>(POSITION_REQUEST);

    public static final StreamCodec<FriendlyByteBuf, PlayerPositionRequest> CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), PlayerPositionRequest::playerUUID,
        ResourceLocation.STREAM_CODEC, PlayerPositionRequest::payloadID,
        PlayerPositionRequest::new
    );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
