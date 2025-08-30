package me.chorus.simpletrackingcompass.hud;

import me.chorus.simpletrackingcompass.util.ModUtils;
import me.chorus.simpletrackingcompass.util.PlayerUtils;
import me.chorus.simpletrackingcompass.util.TrackedPlayer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;

import static me.chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public class CompassHUD {

    // Client references

    private static MinecraftClient client = MinecraftClient.getInstance();
    private static ClientWorld world = client.world;
    private static PlayerEntity player = client.player;

    // Target player being tracked

    private static TrackedPlayer target = null; // by default

    // Compass related variables

    private static final Identifier COMPASS_LAYER = Identifier.of("compass_hud_layer");

    private static int compassTexture = 404;

    private static final double halfRange = 360.0 / (32 * 2);

    // Timing variables

    // TODO: Make these adjustable by the client
    private static final int NEARBY_UPDATE_INTERVAL = 5;
    private static final int REMOTE_UPDATE_INTERVAL = 5;

    private static long tickCounter = 0L;
    private static long lastUpdateTick = 0L;

    // Public booleans

    public static boolean CompassHUDHidden = false;
    public static boolean IsServerModded = false;

    public static void register() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerBefore(IdentifiedLayer.DEBUG, COMPASS_LAYER, ((context, counter) -> {
                    if (isHidden()) return;

                    if (client == null || world == null || player == null) updateClientReferences();
                    if (target == null) updateTrackedPlayer();

                    // Calculate which compass texture to use

                    if (tickCounter - lastUpdateTick >= getUpdateFrequency()) {
                        lastUpdateTick = tickCounter;

                        updateClientReferences();
                        updateTrackedPlayer();

                        int playerX = (int) player.getX();
                        int playerZ = (int) player.getZ();

                        int targetX = (int) target.getX();
                        int targetZ = (int) target.getZ();

                        if ((playerX == targetX && playerZ == targetZ)
                                ||
                                player.getUuid() == target.getUuid()) {
                            compassTexture = 404;
                        }
                        else {
                            int playerYaw = ((int) player.getYaw() % 360 + 360) % 360;

                            int targetYaw = ModUtils.calculateAngle(
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
                    int[] size = ModUtils.scaleTextureToFit(COMPASS_ICON, maxSide);

                    int compassWidth = size[0];
                    int compassHeight = size[1];

                    context.drawTexture(
                            RenderLayer::getGuiTextured,
                            COMPASS_ICON,
                            compassX, compassY,
                            compassU, compassV,
                            compassWidth, compassHeight,
                            compassWidth, compassHeight
                    );

                    // Draw the target player's name below the compass

                    TextRenderer tr = client.textRenderer;
                    int textHeight = tr.fontHeight;

                    String[] lines = {
                            "Tracking:",
                            target.getName()
                    };

                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        int textWidth = tr.getWidth(line);
                        int x = Math.max(0, compassX + (compassWidth - textWidth) / 2);
                        int y = compassY + compassHeight + i * textHeight;

                        context.drawText(tr, Text.literal(line),
                                x, y, 0xFF00FF00,
                                false);
                    }
                }))
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (isHidden()) return;
                    tickCounter++;
                }
        );
    }

    public static void setTargetPlayer(UUID uuid, String name, boolean runtimeTriggered) {
        if (uuid == null || name == null) return;

        PlayerListEntry entry = client.getNetworkHandler() != null ? client.getNetworkHandler().getPlayerListEntry(uuid) : null;
        PlayerEntity playerEntity = PlayerUtils.getPlayerEntity(uuid);

        if (entry != null) {
                if (playerEntity != null) {
                    target = new TrackedPlayer(playerEntity);
                }
                else if (IsServerModded) {
                    target = new TrackedPlayer(uuid, name);
                }
                else {
                    if (runtimeTriggered) resetTarget();
                    PlayerUtils.playerOutOfRenderDistanceWarn(name);
                }
        }
        else {
            if (runtimeTriggered) resetTarget();
            PlayerUtils.playerNotFoundError(name);
        }

        resetTickCounter();
    }

    public static boolean isHidden() {
        return CompassHUDHidden || MinecraftClient.getInstance().options.hudHidden;
    }

    public static TrackedPlayer getTarget() {
        return target;
    }

    public static void onJoin() {
        updateClientReferences();
        resetTarget();
        resetTickCounter();
    }

    // Private helper methods

    private static int getUpdateFrequency() {
        return (target != null && target.getEntity() != null) ? NEARBY_UPDATE_INTERVAL : REMOTE_UPDATE_INTERVAL;
    }

    private static void resetTickCounter() {
        tickCounter = getUpdateFrequency();
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
            setTargetPlayer(target.getUuid(), target.getName(), true);
        }

        if (target.getDimension() != null && !world.getRegistryKey().getValue().equals(target.getDimension())) {
            PlayerUtils.playerInAnotherDimensionWarn(target.getName(), target.getDimension());
            resetTarget();
        }

        target.update();
    }
}