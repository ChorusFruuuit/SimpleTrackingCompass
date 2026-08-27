package net.chorus.simpletrackingcompass;

import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimpleTrackingCompass {
    public static final String MOD_ID = "simpletrackingcompass";
    public static final Logger LOGGER = LoggerFactory.getLogger("Simple Tracking Compass");

    public static final ResourceLocation POSITION_REQUEST = Utils.identifierWithModNamespace("packet/position_request");
    public static final ResourceLocation POSITION_RESPONSE = Utils.identifierWithModNamespace("packet/position_response");

    public static final ResourceLocation CLIENT_COMMANDS = Utils.identifierWithModNamespace("destination/client_commands");
    public static final ResourceLocation TARGET_SELECTOR_SCREEN = Utils.identifierWithModNamespace("destination/target_selector_screen");
    public static final ResourceLocation TRACKED_PLAYER = Utils.identifierWithModNamespace("destination/tracked_player");

    private static boolean serverModded = false;

    /**
     * Whether {@link Minecraft#getCurrentServer() currentServer} has the mod Simple Tracking Compass installed.
     * Always {@code false} when {@snippet : currentServer == null && !localServer; // @link substring="currentServer" target="Minecraft#getCurrentServer()" @link substring="localServer" target="Minecraft#isLocalServer()"}
     * */
    public static boolean isServerModded() {
        return serverModded;
    }

    public static void setServerModded(boolean modded) {
        serverModded = modded;
    }
}
