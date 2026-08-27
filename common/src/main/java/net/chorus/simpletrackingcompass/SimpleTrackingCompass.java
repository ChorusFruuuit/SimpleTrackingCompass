package net.chorus.simpletrackingcompass;

import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimpleTrackingCompass {
    public static final String MOD_ID = "simpletrackingcompass";
    public static final Logger LOGGER = LoggerFactory.getLogger("Simple Tracking Compass");

    public static final Identifier POSITION_REQUEST = Utils.identifierWithModNamespace("packet/position_request");
    public static final Identifier POSITION_RESPONSE = Utils.identifierWithModNamespace("packet/position_response");

    public static final Identifier CLIENT_COMMANDS = Utils.identifierWithModNamespace("destination/client_commands");
    public static final Identifier TARGET_SELECTOR_SCREEN = Utils.identifierWithModNamespace("destination/target_selector_screen");
    public static final Identifier TRACKED_PLAYER = Utils.identifierWithModNamespace("destination/tracked_player");

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

/*
TODO:
 1. Update Fabric Loom to the latest version available at the start of the
    next development cycle (currently 1.16-SNAPSHOT).
 2. Use java.util.concurrent.CompletableFuture in the networking code where
    possible, mainly for cleaner async request handling.
 3. Replace the manual split("=") line parsing in ConfigManager with
    java.util.Properties.
 4. Add an option to keep tracking a player's last known position after
    they stop being trackable
 5. Back-port the mod to 1.20.x
 6. Port the mod to the two remaining loaders, Forge and Quilt
 7. Localize all user-facing strings (use lang/ files.)
 8. Remove the OptionsScreen layout hack in TargetSelectorScreen.init()
    (a screen is built just to read the Done button's coordinates)
 9. Extract inline magic numbers into named constants: compass position (10, 10),
    label offset (-68), proximity epsilon (0.2), arrow offset (-5), and max
    texture size (64).
 10. Add a LOGGER.warn in CompassTracker.onResourceManagerReload() when ARROW_UP and ARROW_DOWN
     have different dimensions, since arrow sizing is currently derived from ARROW_UP only.
     OR
     Move all caching and cache management to a single file.
 */
