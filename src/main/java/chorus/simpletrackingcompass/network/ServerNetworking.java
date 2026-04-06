package chorus.simpletrackingcompass.network;

import chorus.simpletrackingcompass.network.packet.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public class ServerNetworking {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(PlayerPositionRequest.ID, (payload, context) -> {
            ServerPlayer requester = context.player();
            UUID targetUuid = payload.targetUuid();
            @SuppressWarnings("resource")
            ServerPlayer target = requester.level().getServer().getPlayerList().getPlayer(targetUuid);
            if (target != null) {
                Vec3 pos = target.position();
                @SuppressWarnings("resource")
                Identifier dimension = target.level().dimension().identifier();

                PlayerPositionResponse response = new PlayerPositionResponse(pos, dimension);
                ServerPlayNetworking.send(requester, response);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Ping.ID, (_, context) ->
            ServerPlayNetworking.send(context.player(), new Pong())
        );
    }
}