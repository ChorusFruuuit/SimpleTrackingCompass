package chorus.simpletrackingcompass.network;

import com.mojang.brigadier.Command;
import chorus.simpletrackingcompass.config.ConfigManager;
import chorus.simpletrackingcompass.hud.CompassHUD;
import chorus.simpletrackingcompass.network.packet.Ping;
import chorus.simpletrackingcompass.network.packet.Pong;
import chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import chorus.simpletrackingcompass.screen.TargetSelectorScreen;
import chorus.simpletrackingcompass.util.TrackedPlayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class ClientNetworking {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
            dispatcher.register(ClientCommands.literal("selector")
                .executes(_ -> {
                    Minecraft client = Minecraft.getInstance();

                    client.schedule(() ->
                        client.setScreen(new TargetSelectorScreen(null))
                    );

                    return Command.SINGLE_SUCCESS;
                })
            )
        );

        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> {
            ClientPlayNetworking.send(new Ping());
            CompassHUD.handleClientJoin();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) ->
            CompassHUD.isServerModded = false
        );

        ClientLifecycleEvents.CLIENT_STOPPING.register(_ ->
            ConfigManager.save(new ConfigManager.Options(
                ConfigManager.SHOULD_SEND_GUIDE_MESSAGE,
                ConfigManager.UPDATE_INTERVAL
            ))
        );

        ClientPlayNetworking.registerGlobalReceiver(PlayerPositionResponse.ID, (payload, _) -> {
            Vec3 pos = payload.pos();
            Identifier dimension = payload.dimensionId();

            TrackedPlayer tracked = CompassHUD.getTrackedPlayer();
            if (tracked != null) {
                tracked.setRemoteData(dimension, pos);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Pong.ID, (_, _) ->
            CompassHUD.isServerModded = true
        );
    }
}