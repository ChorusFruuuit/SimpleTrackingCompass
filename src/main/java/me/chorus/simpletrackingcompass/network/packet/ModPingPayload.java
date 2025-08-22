package me.chorus.simpletrackingcompass.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static me.chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public record ModPingPayload() implements CustomPayload {
    public static final Id<ModPingPayload> ID = new Id<>(Identifier.of(MOD_ID, "mod_ping"));

    public static final PacketCodec<PacketByteBuf, ModPingPayload> CODEC = PacketCodec.of(
            (buf, payload) -> {},
            buf -> new ModPingPayload()
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
