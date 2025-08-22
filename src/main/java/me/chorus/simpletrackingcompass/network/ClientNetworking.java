package me.chorus.simpletrackingcompass.network;

import me.chorus.simpletrackingcompass.CompassHUD;
import me.chorus.simpletrackingcompass.network.packet.ModPingPayload;
import me.chorus.simpletrackingcompass.network.packet.ModPongPayload;
import me.chorus.simpletrackingcompass.network.packet.PlayerPositionResponsePayload;
import me.chorus.simpletrackingcompass.util.TrackedPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class ClientNetworking {
    public static void init() {

        ClientPlayConnectionEvents.JOIN.register((a, b, c) -> {
            ClientPlayNetworking.send(new ModPingPayload());
            CompassHUD.onJoin();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((a, b) ->
                CompassHUD.IsServerModded = false
        );

        ClientPlayNetworking.registerGlobalReceiver(PlayerPositionResponsePayload.ID, (payload, a) -> {
            Vec3d pos = payload.pos();
            Identifier dimension = payload.dimensionId();

            TrackedPlayer tracked = CompassHUD.getTarget();
            if (tracked != null) {
                tracked.setRemoteData(dimension, pos);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPongPayload.ID, (a, b) ->
            CompassHUD.IsServerModded = true
        );
    }
}