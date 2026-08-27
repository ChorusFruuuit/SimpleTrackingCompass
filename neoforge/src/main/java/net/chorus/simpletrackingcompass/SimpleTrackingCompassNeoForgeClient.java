package net.chorus.simpletrackingcompass;

import net.chorus.simpletrackingcompass.command.ClientCommands;
import net.chorus.simpletrackingcompass.event.ClientEvents;
import net.chorus.simpletrackingcompass.network.ClientNetworking;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.chorus.simpletrackingcompass.screen.CompassButtonFactory;
import net.chorus.simpletrackingcompass.screen.widget.TexturedButton;
import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public class SimpleTrackingCompassNeoForgeClient {
    private static final Identifier COMPASS = Utils.identifierWithModNamespace("layer/compass");
    private static final Identifier RELOAD = Utils.identifierWithModNamespace("texture_reloader");

    private static final List<Entity> pendingJoins = new ArrayList<>();

    private static final Predicate<Entity> presenceInTheWorldTest = entity -> {
        if (client().level == null) return false;
        for (Entity comparator : client().level.entitiesForRendering()) {
            if (entity.equals(comparator)) return true;
        }
        return false;
    };

    public static void init(IEventBus eventBus) {
        SimpleTrackingCompassClient.loadConfig(FMLPaths.CONFIGDIR.get());

        // client network registration
        eventBus.addListener(SimpleTrackingCompassNeoForgeClient::registerClientPayloads);
        ClientNetworking.setChannelPresenceTest(channelName ->
            client().getConnection() != null && client().getConnection().hasChannel(channelName)
        );

        // setup and registration
        eventBus.addListener(SimpleTrackingCompassNeoForgeClient::extractRenderState);
        eventBus.addListener(SimpleTrackingCompassNeoForgeClient::onClientResourceReload);

        // runtime events
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::onEndTick);
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::onEntityLoad);
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::onEntityUnload);
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::onClientJoin);
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::onClientDisconnect);
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::onClientStopping);
        NeoForge.EVENT_BUS.addListener(SimpleTrackingCompassNeoForgeClient::afterScreenInit);
    }

    private static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(
            PlayerPositionResponse.TYPE,
            (payload, context) -> ClientNetworking.onPlayerPositionResponseReceive(payload)
        );
    }

    private static void onEndTick(ClientTickEvent.Post event) {
        if (!pendingJoins.isEmpty()) {
            Iterator<Entity> iterator = pendingJoins.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (presenceInTheWorldTest.test(entity)) {
                    ClientEvents.onClientEntityLoad(entity);
                    iterator.remove();
                }
            }
        }
        CompassTracker.onEndTick();
    }

    private static void extractRenderState(RegisterGuiLayersEvent event) {
        event.registerBelowAll(
            COMPASS,
            (graphics, timer) -> CompassTracker.extractRenderState(graphics)
        );
    }

    private static void onClientResourceReload(AddClientReloadListenersEvent event) {
        event.addListener(RELOAD, RELOAD_LISTENER);
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        ClientCommands.register(event.getDispatcher());
    }

    private static void onEntityLoad(EntityJoinLevelEvent event) {
        pendingJoins.add(event.getEntity());
    }

    private static void onEntityUnload(EntityLeaveLevelEvent event) {
        if (pendingJoins.remove(event.getEntity())) return;
        ClientEvents.onClientEntityUnload(event.getEntity());
    }

    private static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientEvents.onClientJoin();
    }

    private static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientEvents.onClientDisconnect();
    }

    private static void onClientStopping(ClientStoppingEvent event) {
        ClientEvents.onClientStopping();
    }

    private static void afterScreenInit(ScreenEvent.Init.Post event) {
        TexturedButton compassButton = CompassButtonFactory.afterScreenInit(event.getScreen());
        if (compassButton != null) event.addListener(compassButton);
    }
}
