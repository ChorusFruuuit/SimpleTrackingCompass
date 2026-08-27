package net.chorus.simpletrackingcompass.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface PacketDestination {
    void processPacket(CustomPacketPayload packetPayload);
}
