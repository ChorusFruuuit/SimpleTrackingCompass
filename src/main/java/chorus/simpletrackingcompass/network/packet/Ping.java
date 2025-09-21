package chorus.simpletrackingcompass.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public record Ping() implements CustomPayload {
    public static final Id<Ping> ID = new Id<>(Identifier.of(MOD_ID, "mod_ping"));

    public static final PacketCodec<PacketByteBuf, Ping> CODEC = PacketCodec.of(
        (payload, buf) -> {
        },
        buf -> new Ping()
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
