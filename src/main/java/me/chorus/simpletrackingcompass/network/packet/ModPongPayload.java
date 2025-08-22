package me.chorus.simpletrackingcompass.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static me.chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public record ModPongPayload() implements CustomPayload {
    public static final Id<ModPongPayload> ID = new Id<>(Identifier.of(MOD_ID, "mod_pong"));

    public static final PacketCodec<PacketByteBuf, ModPongPayload> CODEC = PacketCodec.of(
            (buf, payload) -> {},
            buf -> new ModPongPayload()
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
