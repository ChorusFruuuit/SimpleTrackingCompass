package net.chorus.simpletrackingcompass.event;

import net.chorus.simpletrackingcompass.CompassTracker;
import net.chorus.simpletrackingcompass.config.ConfigManager;
import net.chorus.simpletrackingcompass.network.ClientNetworking;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.chorus.simpletrackingcompass.screen.TargetSelectorScreen;
import net.chorus.simpletrackingcompass.util.PlayerUtils;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;
import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public final class ClientEvents {
    public static void onClientEntityLoad(Entity entity) {
        if (!(entity instanceof Player player) || !isServerModded() || CompassTracker.getTarget() == null
            || CompassTracker.getTarget().getPlayer() != null) return;

        if (CompassTracker.getTarget().getGameProfile().getId().equals(player.getUUID())) {
            CompassTracker.resolveLocal(player, true);
        }
    }

    public static void onClientEntityUnload(Entity entity) {
        if (!(entity instanceof Player player) || CompassTracker.getTarget() == null) return;

        if (CompassTracker.getTarget().getGameProfile().getId().equals(player.getUUID())) {
            CompassTracker.resolveRemote(PlayerUtils.getOnlinePlayer(player.getUUID()), true);
        }
    }

    public static void onClientJoin() {
        setServerModded(ClientNetworking.canSend(PlayerPositionRequest.TYPE.id()));
        CompassTracker.onClientJoin();

        if (!isServerModded()) {
            ServerData currentServer = client().getCurrentServer();
            LOGGER.info(
                "The server {} doesn't have the mod Simple Tracking Compass installed.",
                currentServer != null
                    ? currentServer.ip
                    : (client().hasSingleplayerServer() ? "LAN world" : "unknown server")
            );
        }

        if (!ConfigManager.hasSeenGuideMessage && client().player != null) {
            client().gui.getChat().addMessage(PlayerUtils.STCGuideMessage());
            ConfigManager.hasSeenGuideMessage = true;
        }
    }

    public static void onClientDisconnect() {
        setServerModded(false);
        TargetSelectorScreen.setDoTrackabilityTest(false);
    }

    public static void onClientStopping() {
        ConfigManager.save(new ConfigManager.ConfigOptions(
            ConfigManager.hasSeenGuideMessage,
            ConfigManager.packetInterval
        ));
    }
}
