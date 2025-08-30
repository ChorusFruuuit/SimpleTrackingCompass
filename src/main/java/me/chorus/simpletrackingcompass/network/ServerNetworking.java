package me.chorus.simpletrackingcompass.network;

import me.chorus.simpletrackingcompass.network.packet.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class ServerNetworking {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(PlayerPositionRequest.ID, (payload, context) -> {
            ServerPlayerEntity requester = context.player();
            UUID targetUuid = payload.targetUuid();
            ServerPlayerEntity target = requester.getServer() != null ? requester.getServer().getPlayerManager().getPlayer(targetUuid) : null;
            if (target != null) {
                Vec3d pos = target.getPos();
                Identifier dimension = target.getWorld().getRegistryKey().getValue();

                var response = new PlayerPositionResponse(pos, dimension);
                ServerPlayNetworking.send(requester, response);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Ping.ID, (a, context) ->
            ServerPlayNetworking.send(context.player(), new Pong())
        );
    }
}