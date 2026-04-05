package chorus.simpletrackingcompass.network;

import com.mojang.brigadier.Command;
import chorus.simpletrackingcompass.config.ConfigManager;
import chorus.simpletrackingcompass.hud.CompassHUD;
import chorus.simpletrackingcompass.network.packet.Ping;
import chorus.simpletrackingcompass.network.packet.Pong;
import chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import chorus.simpletrackingcompass.screen.TargetSelectorScreen;
import chorus.simpletrackingcompass.util.TrackedPlayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class ClientNetworking {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("selector")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();

                    client.send(() ->
                        client.setScreen(new TargetSelectorScreen(null))
                    );

                    return Command.SINGLE_SUCCESS;
                })
            )
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworking.send(new Ping());
            CompassHUD.handleClientJoin();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            CompassHUD.isServerModded = false
        );

        ClientLifecycleEvents.CLIENT_STOPPING.register(client ->
            ConfigManager.save(new ConfigManager.Options(
                ConfigManager.SHOULD_SEND_GUIDE_MESSAGE,
                ConfigManager.UPDATE_INTERVAL
            ))
        );

        ClientPlayNetworking.registerGlobalReceiver(PlayerPositionResponse.ID, (payload, context) -> {
            Vec3d pos = payload.pos();
            Identifier dimension = payload.dimensionId();

            TrackedPlayer tracked = CompassHUD.getTrackedPlayer();
            if (tracked != null) {
                tracked.setRemoteData(dimension, pos);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Pong.ID, (payload, context) ->
            CompassHUD.isServerModded = true
        );
    }
}