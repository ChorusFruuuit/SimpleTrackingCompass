package chorus.simpletrackingcompass.hud;

import chorus.simpletrackingcompass.config.ConfigManager;
import chorus.simpletrackingcompass.mixin.InGameHudAccessor;
import chorus.simpletrackingcompass.util.Utils;
import chorus.simpletrackingcompass.util.PlayerUtils;
import chorus.simpletrackingcompass.util.TrackedPlayer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public class CompassHUD {

    // Client references

    private static MinecraftClient client = MinecraftClient.getInstance();
    private static ClientWorld world = client.world;
    private static PlayerEntity player = client.player;

    // Target player being tracked

    private static TrackedPlayer target = null; // by default

    // Compass related variables

    private static final Identifier COMPASS_ID = Identifier.of("compass_hud");

    private static int compassTexture = 404;

    private static final double halfRange = 360.0 / (32 * 2);

    // Timing variables

    private static long tickCounter = 0L;
    private static long lastUpdateTick = 0L;

    // Public booleans

    public static boolean CompassHUDHidden = false;
    public static boolean IsServerModded = false;

    public static void register() {
        HudElementRegistry.addLast(
            COMPASS_ID,
            // This method is called every tick to render the compass HUD
            (context, counter) -> {

                if (isHidden()) return;

                if (client == null || world == null || player == null) updateClientReferences();
                if (target == null) updateTrackedPlayer();

                // Calculate which compass texture to use

                if (tickCounter - lastUpdateTick >= ConfigManager.UPDATE_INTERVAL) {
                    lastUpdateTick = tickCounter;

                    updateClientReferences();
                    updateTrackedPlayer();

                    int playerX = (int) player.getX();
                    int playerZ = (int) player.getZ();

                    int targetX = (int) target.getX();
                    int targetZ = (int) target.getZ();

                    if ((playerX == targetX && playerZ == targetZ) ||
                        player.getUuid() == target.getUuid()) {
                        compassTexture = 404;
                    } else {
                        int playerYaw = ((int) player.getYaw() % 360 + 360) % 360;

                        int targetYaw = Utils.calculateAngle(
                            playerX, playerZ,
                            targetX, targetZ
                        );

                        int angle = ((180 + (targetYaw - playerYaw)) % 360 + 360) % 360;

                        compassTexture = (int) ((angle + halfRange) / (2 * halfRange));
                        compassTexture %= 32;
                    }
                }

                Identifier COMPASS_ICON = Identifier.of(
                    MOD_ID,
                    "textures/gui/compass_" + compassTexture + ".png"
                );

                // Draw the compass icon on the HUD

                final int compassX = 10, compassY = 10;
                final int compassU = 0, compassV = 0;
                final int maxSide = 64; // px
                int[] size = Utils.scaleTextureToFit(COMPASS_ICON, maxSide);

                int compassWidth = size[0];
                int compassHeight = size[1];

                context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    COMPASS_ICON,
                    compassX, compassY,
                    compassU, compassV,
                    compassWidth, compassHeight,
                    compassWidth, compassHeight
                );

                // Draw the target player's name above the Hotbar

                InGameHudAccessor inGameHud = ((InGameHudAccessor) client.inGameHud);
                if (inGameHud.getOverlayMessage() != null &&
                    inGameHud.getOverlayRemaining() > 0) return;

                TextRenderer tr = client.textRenderer;
                String text = "Currently tracking: " + target.getName();

                int x = context.getScaledWindowWidth() / 2 - tr.getWidth(text) / 2;
                int y = context.getScaledWindowHeight() - 68 - tr.fontHeight / 2;

                context.drawText(tr, text, x, y, 0xFFFFFFFF, true);
            }
        );

        ClientTickEvents.END_CLIENT_TICK.register(
            client -> {
                if (isHidden()) return;
                tickCounter++;
            }
        );
    }

    public static void setTarget(UUID uuid, String name, boolean runtimeTriggered) {
        if (uuid == null || name == null) return;

        PlayerListEntry entry = client.getNetworkHandler() != null ? client.getNetworkHandler().getPlayerListEntry(uuid) : null;
        PlayerEntity playerEntity = PlayerUtils.getPlayerEntity(uuid);

        if (entry != null) {
            if (playerEntity != null) {
                target = new TrackedPlayer(playerEntity);
            } else if (IsServerModded) {
                target = new TrackedPlayer(uuid, name);
            } else {
                if (runtimeTriggered) resetTarget();
                PlayerUtils.playerOutOfRenderDistanceWarn(name);
            }
        } else {
            if (runtimeTriggered) resetTarget();
            PlayerUtils.playerNotFoundError(name);
        }

        resetTickCounter();
    }

    public static TrackedPlayer getTarget() {
        return target;
    }

    public static void onJoin() {
        updateClientReferences();
        resetTarget();
        resetTickCounter();

        if (ConfigManager.SHOULD_SEND_GUIDE_MESSAGE) {
            PlayerUtils.guideMessage();
            ConfigManager.SHOULD_SEND_GUIDE_MESSAGE = false;
        }
    }

    // Private helper methods

    private static boolean isHidden() {
        return CompassHUDHidden || MinecraftClient.getInstance().options.hudHidden;
    }

    private static void resetTickCounter() {
        tickCounter = ConfigManager.UPDATE_INTERVAL;
        lastUpdateTick = 0L;
    }

    private static void resetTarget() {
        target = MinecraftClient.getInstance().player != null ? new TrackedPlayer(MinecraftClient.getInstance().player) : null;
    }

    private static void updateClientReferences() {
        client = MinecraftClient.getInstance();
        world = client.world;
        player = client.player;
    }

    private static void updateTrackedPlayer() {
        if (target == null) resetTarget();

        Boolean withinRenderDistance = PlayerUtils.isWithinRenderDistance(target.getUuid());
        if ((withinRenderDistance == null) || (withinRenderDistance && target.getEntity() == null) || (!withinRenderDistance && target.getEntity() != null)) {
            setTarget(target.getUuid(), target.getName(), true);
        }

        if (target.getDimension() != null && !world.getRegistryKey().getValue().equals(target.getDimension())) {
            PlayerUtils.playerInAnotherDimensionWarn(target.getName(), target.getDimension());
            resetTarget();
        }

        target.update();
    }
}