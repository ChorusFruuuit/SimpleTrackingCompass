package net.chorus.simpletrackingcompass.network.packet;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;

public record PlayerPositionResponse(Optional<UUID> playerUUID, Optional<Vec3> playerPosition, Optional<ResourceKey<Level>> playerDimension, ResourceLocation payloadID) implements CustomPacketPayload {
    public static final Type<PlayerPositionResponse> TYPE = new Type<>(POSITION_RESPONSE);

    public static final StreamCodec<FriendlyByteBuf, PlayerPositionResponse> CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), PlayerPositionResponse::playerUUID,
        ByteBufCodecs.optional(Vec3.STREAM_CODEC), PlayerPositionResponse::playerPosition,
        ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), PlayerPositionResponse::playerDimension,
        ResourceLocation.STREAM_CODEC, PlayerPositionResponse::payloadID,
        PlayerPositionResponse::new
    );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
