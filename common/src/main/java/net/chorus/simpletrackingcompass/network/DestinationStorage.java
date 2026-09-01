package net.chorus.simpletrackingcompass.network;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class DestinationStorage {
    private static final Map<ResourceLocation, PacketDestination> destinations = new HashMap<>();

    public static PacketDestination put(ResourceLocation id, PacketDestination destination) {
        return destinations.put(id, destination);
    }

    public static PacketDestination get(ResourceLocation id) {
        return destinations.get(id);
    }

    public static PacketDestination remove(ResourceLocation id) {
        return destinations.remove(id);
    }
}
