package net.chorus.simpletrackingcompass.network;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class DestinationStorage {
    private static final Map<Identifier, PacketDestination> destinations = new HashMap<>();

    public static PacketDestination put(Identifier id, PacketDestination destination) {
        return destinations.put(id, destination);
    }

    public static PacketDestination get(Identifier id) {
        return destinations.get(id);
    }

    public static PacketDestination remove(Identifier id) {
        return destinations.remove(id);
    }
}
