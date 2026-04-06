package chorus.simpletrackingcompass.network.packet;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record Pong() implements CustomPacketPayload {
    public static final Type<Pong> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "mod_pong"));

    public static final StreamCodec<FriendlyByteBuf, Pong> CODEC = StreamCodec.ofMember(
        (_, _) -> {
        },
        _ -> new Pong()
    );

    @Override
    @NonNull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
