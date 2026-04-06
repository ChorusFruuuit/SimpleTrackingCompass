package chorus.simpletrackingcompass.hud;

import chorus.simpletrackingcompass.config.ConfigManager;
import chorus.simpletrackingcompass.mixin.InGameHudAccessor;
import chorus.simpletrackingcompass.util.Utils;
import chorus.simpletrackingcompass.util.PlayerUtils;
import chorus.simpletrackingcompass.util.TrackedPlayer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public class CompassHUD {

    // Client references

    private static Minecraft client = Minecraft.getInstance();
    private static ClientLevel clientWorld = client.level;
    private static Player localPlayer = client.player;

    // The player currently being tracked

    private static TrackedPlayer trackedPlayer = null;

    // Compass HUD rendering state and constants

    private static final Identifier HUD_COMPASS_ID = Identifier.parse("compass_hud");

    private static final double halfFrameAngle = 360.0 / (32 * 2);

    /**
     * Index of the current compass frame texture.
     * Normal values: 0–31 → needle direction (one frame per 11.25°).
     * Special value: 32 → "self-tracking" state (dot instead of needle).
     * Updated every {@link ConfigManager#UPDATE_INTERVAL} ticks in the client tick handler.
     * */
    private static int compassFrameIndex = 32;
    private static String trackingLabel = "";

    // Timing variables

    private static long gameTicks = 0L;
    private static long lastUpdateTick = 0L;

    // Public booleans

    public static boolean isCompassHUDHidden = false;
    public static boolean isServerModded = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(
            _ -> {
                if (isHidden() || !PlayerUtils.inWorld()) return;
                gameTicks++;

                // Calculate which compass texture to use

                updateClientReferences();
                updateTrackedPlayer();

                int playerX = (int) localPlayer.getX();
                int playerZ = (int) localPlayer.getZ();

                int trackedPlayerX = (int) trackedPlayer.getX();
                int trackedPlayerZ = (int) trackedPlayer.getZ();

                if (localPlayer.getUUID().equals(trackedPlayer.getUuid()) ||
                    (playerX == trackedPlayerX && playerZ == trackedPlayerZ)) {
                    compassFrameIndex = 32;
                } else {
                    int playerYaw = ((int) localPlayer.getYRot() % 360 + 360) % 360;

                    int trackedPlayerYaw = Utils.calculateAngle(
                        playerX, playerZ,
                        trackedPlayerX, trackedPlayerZ
                    );

                    int angle = ((180 + (trackedPlayerYaw - playerYaw)) % 360 + 360) % 360;

                    compassFrameIndex = (int) ((angle + halfFrameAngle) / (2 * halfFrameAngle));
                    compassFrameIndex %= 32;
                }

                trackingLabel = "Currently tracking: " + trackedPlayer.getName();
            }
        );

        HudElementRegistry.addFirst(
            HUD_COMPASS_ID,
            // This method is called every tick to render the compass HUD
            (context, _) -> {
                if (isHidden()) return;

                if (client == null) client = Minecraft.getInstance();

                Identifier compassTextureId = Identifier.fromNamespaceAndPath(
                    MOD_ID,
                    "textures/gui/compass_" + compassFrameIndex + ".png"
                );

                // Draw the compass icon on the HUD

                final int hudCompassX = 10, hudCompassY = 10;
                final int textureU = 0, textureV = 0;
                final int maxCompassSize = 64; // px
                int[] size = Utils.scaleTextureToFit(compassTextureId, maxCompassSize);

                int scaledWidth = size[0];
                int scaledHeight = size[1];

                context.blit(
                    RenderPipelines.GUI_TEXTURED,
                    compassTextureId,
                    hudCompassX, hudCompassY,
                    textureU, textureV,
                    scaledWidth, scaledHeight,
                    scaledWidth, scaledHeight
                );

                // Draw the trackedPlayer's name above the Hotbar

                InGameHudAccessor inGameHud = ((InGameHudAccessor) client.gui);
                if (inGameHud.getOverlayMessage() != null &&
                    inGameHud.getOverlayRemaining() > 0) return;

                Font tr = client.font;

                int x = context.guiWidth() / 2 - tr.width(trackingLabel) / 2;
                int y = context.guiHeight() - 68 - tr.lineHeight / 2;

                context.text(tr, trackingLabel, x, y, 0xFFFFFFFF, true);
            }
        );
    }

    public static void setTrackedPlayer(UUID uuid, String name, boolean runtimeTriggered) {
        if (uuid == null || name == null) return;

        PlayerInfo entry = client.getConnection() != null ? client.getConnection().getPlayerInfo(uuid) : null;
        Player playerEntity = PlayerUtils.getPlayerEntity(uuid);

        if (entry != null) {
            if (playerEntity != null) {
                trackedPlayer = new TrackedPlayer(playerEntity);
            } else if (isServerModded) {
                trackedPlayer = new TrackedPlayer(uuid, name);
            } else {
                if (runtimeTriggered) resetTrackedPlayer();
                PlayerUtils.playerOutOfRenderDistanceWarn(name);
            }
        } else {
            if (runtimeTriggered) resetTrackedPlayer();
            PlayerUtils.playerNotFoundError(name);
        }

        syncTickCounter();
    }

    public static TrackedPlayer getTrackedPlayer() {
        return trackedPlayer;
    }

    public static void handleClientJoin() {
        updateClientReferences();
        resetTrackedPlayer();
        syncTickCounter();

        if (ConfigManager.SHOULD_SEND_GUIDE_MESSAGE) {
            PlayerUtils.guideMessage();
            ConfigManager.SHOULD_SEND_GUIDE_MESSAGE = false;
        }
    }

    // Private helper methods

    private static boolean isHidden() {
        return isCompassHUDHidden || Minecraft.getInstance().options.hideGui;
    }

    private static void syncTickCounter() {
        gameTicks = ConfigManager.UPDATE_INTERVAL;
        lastUpdateTick = 0L;
    }

    private static void resetTrackedPlayer() {
        trackedPlayer = Minecraft.getInstance().player != null ? new TrackedPlayer(Minecraft.getInstance().player) : null;
    }

    private static void updateClientReferences() {
        clientWorld = client.level;
        localPlayer = client.player;
    }

    private static void updateTrackedPlayer() {
        if (trackedPlayer == null) resetTrackedPlayer();

        Boolean withinRenderDistance = PlayerUtils.isWithinRenderDistance(trackedPlayer.getUuid());
        if ((withinRenderDistance == null) || (withinRenderDistance && trackedPlayer.getEntity() == null) || (!withinRenderDistance && trackedPlayer.getEntity() != null)) {
            setTrackedPlayer(trackedPlayer.getUuid(), trackedPlayer.getName(), true);
        }

        if (trackedPlayer.getDimension() != null && !clientWorld.dimension().identifier().equals(trackedPlayer.getDimension())) {
            PlayerUtils.playerInAnotherDimensionWarn(trackedPlayer.getName(), trackedPlayer.getDimension());
            resetTrackedPlayer();
        }

        if (withinRenderDistance == null || withinRenderDistance) {
            trackedPlayer.update();
            return;
        }

        if (gameTicks - lastUpdateTick >= ConfigManager.UPDATE_INTERVAL) {
            lastUpdateTick = gameTicks;
            trackedPlayer.update();
        }
    }
}