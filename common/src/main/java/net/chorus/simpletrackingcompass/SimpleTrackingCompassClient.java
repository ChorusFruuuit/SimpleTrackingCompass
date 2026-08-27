package net.chorus.simpletrackingcompass;

import net.chorus.simpletrackingcompass.config.ConfigManager;
import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.nio.file.Path;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;

public final class SimpleTrackingCompassClient {
    public static final Identifier COMPASS_STATIC = Utils.identifierWithModNamespace("textures/hud/compass_static.png");
    public static final Identifier ARROW_UP = Utils.identifierWithModNamespace("textures/hud/arrow_up.png");
    public static final Identifier ARROW_DOWN = Utils.identifierWithModNamespace("textures/hud/arrow_down.png");

    public static final Identifier FILTER = Utils.identifierWithModNamespace("textures/gui/filter.png");
    public static final Identifier REFRESH = Utils.identifierWithModNamespace("textures/gui/refresh.png");

    public static final ResourceManagerReloadListener RELOAD_LISTENER =
        SimpleTrackingCompassClient::onResourceManagerReload;

    private static ResourceManager resourceManager = null;

    public static void loadConfig(Path configDir) {
        ConfigManager.setConfigDir(configDir);

        ConfigManager.ConfigOptions options = ConfigManager.load();
        ConfigManager.hasSeenGuideMessage = options.hasSeenGuideMessage();
        ConfigManager.packetInterval = Math.clamp(options.packetInterval(), 1, 40);
    }

    private static void onResourceManagerReload(ResourceManager resourceManager) {
        try {
            SimpleTrackingCompassClient.resourceManager = resourceManager;
            CompassTracker.onResourceManagerReload();
        } catch (Throwable t) {
            LOGGER.warn("Failed to reload the mod's assets", t);
            return;
        }

        LOGGER.info("Successfully reloaded the mod's assets");
    }

    public static Minecraft client() {
        return Minecraft.getInstance();
    }

    public static ResourceManager resourceManager() {
        return resourceManager;
    }
}
    