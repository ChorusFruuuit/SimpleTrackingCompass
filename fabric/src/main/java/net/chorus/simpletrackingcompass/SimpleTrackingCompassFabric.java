package net.chorus.simpletrackingcompass;

import net.chorus.simpletrackingcompass.command.ClientCommands;
import net.chorus.simpletrackingcompass.event.ClientEvents;
import net.chorus.simpletrackingcompass.network.ClientNetworking;
import net.chorus.simpletrackingcompass.network.ServerNetworking;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.chorus.simpletrackingcompass.screen.CompassButtonFactory;
import net.chorus.simpletrackingcompass.screen.widget.TexturedButton;
import net.chorus.simpletrackingcompass.util.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public class SimpleTrackingCompassFabric implements ModInitializer, ClientModInitializer {
    private static final ResourceLocation COMPASS = Utils.identifierWithModNamespace("layer/compass");
    private static final ResourceLocation RELOAD = Utils.identifierWithModNamespace("texture_reloader");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(PlayerPositionRequest.TYPE, PlayerPositionRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerPositionResponse.TYPE, PlayerPositionResponse.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
            PlayerPositionRequest.TYPE,
            (payload, context) ->
                ServerNetworking.onPlayerPositionRequestReceive(payload, context.player())
        );

        // ServerPlayNetworking#canSend isn't reliable enough in some scenarios
        ServerNetworking.setChannelPresenceTest((packetListener, channelId) ->
            packetListener != null
        );
    }

    @Override
    public void onInitializeClient() {
        SimpleTrackingCompassClient.loadConfig(FabricLoader.getInstance().getConfigDir());

        ClientPlayNetworking.registerGlobalReceiver(
            PlayerPositionResponse.TYPE,
            (payload, context) -> ClientNetworking.onPlayerPositionResponseReceive(payload)
        );

        ClientNetworking.setChannelPresenceTest(channelId ->
            client().getConnection() != null && ClientPlayNetworking.canSend(channelId)
        );

        ClientTickEvents.END_CLIENT_TICK.register(
            client -> CompassTracker.onEndTick()
        );
        HudElementRegistry.addFirst(
            COMPASS, (graphics, timer) -> CompassTracker.extractRenderState(graphics)
        );

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
            RELOAD, RELOAD_LISTENER
        );

        ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, buildContext) ->
                ClientCommands.register(dispatcher)
        );

        ClientEntityEvents.ENTITY_LOAD.register(
            (entity, clientLevel) -> ClientEvents.onClientEntityLoad(entity)
        );
        ClientEntityEvents.ENTITY_UNLOAD.register(
            (entity, clientLevel) -> ClientEvents.onClientEntityUnload(entity)
        );

        ClientPlayConnectionEvents.JOIN.register(
            (packetListener, sender, client) -> ClientEvents.onClientJoin()
        );
        ClientPlayConnectionEvents.DISCONNECT.register(
            (packetListener, client) -> ClientEvents.onClientDisconnect()
        );
        ClientLifecycleEvents.CLIENT_STOPPING.register(
            client -> ClientEvents.onClientStopping()
        );

        ScreenEvents.AFTER_INIT.register(
            (client, screen, scaledWidth, scaledHeight) -> {
                TexturedButton compassButton = CompassButtonFactory.afterScreenInit(screen);
                if (compassButton != null) Screens.getButtons(screen).add(compassButton);
            }
        );
    }
}
