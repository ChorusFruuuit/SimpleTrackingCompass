package me.chorus.simpletrackingcompass.network;

import me.chorus.simpletrackingcompass.network.packet.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class ServerNetworking {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(PlayerPositionRequestPayload.ID, (payload, context) -> {
            ServerPlayerEntity requester = context.player();
            UUID targetUuid = payload.targetUuid();
            ServerPlayerEntity target = requester.getServer() != null ? requester.getServer().getPlayerManager().getPlayer(targetUuid) : null;
            if (target != null) {
                Vec3d pos = target.getPos();
                Identifier dimension = target.getWorld().getRegistryKey().getValue();
//                MinecraftClient.getInstance().player.sendMessage(Text.literal("<ServerNetworking> " + dimension), false);

                var response = new PlayerPositionResponsePayload(pos, dimension);
                ServerPlayNetworking.send(requester, response);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ModPingPayload.ID, (a, context) ->
            ServerPlayNetworking.send(context.player(), new ModPongPayload())
        );
    }
}