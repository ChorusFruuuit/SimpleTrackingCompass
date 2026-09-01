package net.chorus.simpletrackingcompass;

import com.mojang.authlib.GameProfile;
import net.chorus.simpletrackingcompass.config.ConfigManager;
import net.chorus.simpletrackingcompass.mixin.GuiAccessor;
import net.chorus.simpletrackingcompass.util.PlayerUtils;
import net.chorus.simpletrackingcompass.util.TrackedPlayer;
import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;
import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

/**
 * The main class. This is the core system of this mod.
 * The logic regarding compass tracking is performed here (compass bearing calculations, target resolution).
 * The rendering also happens here and consists of three stages:
 *  Compass rendering, Arrow rendering and Tracking Label rendering
 * */
public final class CompassTracker {

    // The player currently being tracked

    private static TrackedPlayer target = null;

    // Constants

    private static final int COMPASS_STATIC_FRAME = 32;
    private static final int MAX_COMPASS_TEXTURE_SIDE_SIZE = 64;
    private static final int MAX_ARROW_TEXTURE_SIDE_SIZE = 20;

    private static final double FRAME_ANGLE_DEG = 360.0 / 32.0;
    private static final double HALF_FRAME_ANGLE_DEG = FRAME_ANGLE_DEG / 2;

    // Rendering state

    private static boolean compassHidden = false;
    /**
     * There are 32 possible compass frames (0 to 31)
     * each represented by a needle position.
     * Note, that there is also a special 33rd frame
     * where the needle is represented by a dot.
     * This frame is shown whenever the local player
     * tracks themselves or stays inside the target.
     * */
    private static int compassFrame = COMPASS_STATIC_FRAME;
    private static ResourceLocation compassTexture = currentTexture();
    private static final int[] compassPos = new int[]{10, 10};
    private static final int[] compassSize = new int[]{0, 0};

    private static ResourceLocation arrowTexture = null;
    private static final int[] arrowPos = new int[]{0, 0};
    private static final int[] arrowSize = new int[]{0, 0};

    private static Component trackingLabel = Component.empty();
    private static final int[] labelPos = new int[]{0, 0};

    private static int cachedGuiWidth = 0;
    private static int cachedGuiHeight = 0;

    // Tick counter

    private static long gameTicks = 0L;

    // Called at the end of client tick

    public static void onEndTick() {
        if (compassHidden || client().options.hideGui || client().player == null) return;
        gameTicks++;

        // Calculate which compass texture to render

        if (target == null || isSelfTracking()) {
            compassFrame = COMPASS_STATIC_FRAME;
            arrowTexture = null;
        } else {
            ensureTrackable();
            if (target.getPlayer() != null || gameTicks % ConfigManager.packetInterval == 0) target.update();

            if (isClientInsideTargetHorizontally()) {
                compassFrame = COMPASS_STATIC_FRAME;
            } else {
                double localX = client().player.getX(), targetX = target.getX();
                double localZ = client().player.getZ(), targetZ = target.getZ();

                double standardizedLocalYRot = Utils.toGeometricAngle(client().player.getYRot());
                double angleToTarget = Utils.normalizedAngle(
                    // Because Minecraft's Z-axis is inverted (minecraftZ = -mathY),
                    // we use -(targetZ - localZ) instead of (targetZ - localZ).
                    Math.toDegrees(Math.atan2(-(targetZ - localZ), targetX - localX))
                );

                double needleAngle = Utils.toMinecraftAngle(
                    // Shift by 90 degrees to convert the geometric angle (0° East)
                    // to compass heading (0° North).
                    Utils.normalizedAngle(angleToTarget - standardizedLocalYRot) + 90
                );

                compassFrame = extractAppropriateCompassFrame(needleAngle);
            }

            double localY = client().player.getY(), targetY = target.getY();

            if (localY > targetY - 0.2 && localY < targetY + 0.2) {
                arrowTexture = null;
            } else if (localY > targetY) {
                arrowTexture = ARROW_DOWN;
                // The compass height doesn't actually take 64 (16 * 4) pixels
                arrowPos[1] = compassPos[1] + compassSize[1] - 5;
            } else if (localY < targetY) {
                arrowTexture = ARROW_UP;
                // The compass height doesn't actually take 64 (16 * 4) pixels
                arrowPos[1] = compassPos[1] + 5 - arrowSize[1] / 2;
            }
        }

        compassTexture = currentTexture();

        Component oldTrackingLabel = trackingLabel;
        trackingLabel = target != null
            ? Component.literal("Currently tracking: " + target.getGameProfile().getName())
            : Component.literal("No target selected!");

        if (!oldTrackingLabel.equals(trackingLabel)) recalculateLabelPosition();
    }

    // Called every frame

    public static void extractRenderState(GuiGraphics graphics) {
        if (compassHidden || client().options.hideGui || client().player == null) return;

        // Render the appropriate compass texture to the hud

        graphics.pose().pushPose();

        graphics.blit(
            RenderType::guiTextured, compassTexture,
            compassPos[0], compassPos[1], 0F, 0F,
            compassSize[0], compassSize[1],
            compassSize[0], compassSize[1]
        );

        // Render the appropriate arrow texture to the hud

        if (arrowTexture != null) {
            graphics.blit(
                RenderType::guiTextured, arrowTexture,
                arrowPos[0], arrowPos[1], 0F, 0F,
                arrowSize[0], arrowSize[1] / 2,
                arrowSize[0], arrowSize[1]
            );
        }

        // Render the target's name above the hotbar

        GuiAccessor guiAccessor = Utils.getGuiAccessor();
        if (guiAccessor.getOverlayMessageString() == null || guiAccessor.getOverlayMessageTime() <= 0) {
            Font font = Utils.getFont();

            if (graphics.guiWidth() != cachedGuiWidth || graphics.guiHeight() != cachedGuiHeight) {
                cachedGuiWidth = graphics.guiWidth();
                cachedGuiHeight = graphics.guiHeight();

                recalculateLabelPosition();
            }

            graphics.drawString(font, trackingLabel, labelPos[0], labelPos[1], 0xFFFFFFFF, true);
        }

        graphics.pose().popPose();
    }

    public static TrackedPlayer getTarget() {
        return target;
    }

    public static void resolveLocal(Player player, boolean resetOnFailure) {
        if (player == null) return;

        Boolean withinRenderDistance = PlayerUtils.isWithinRenderDistance(player.getUUID());

        if (withinRenderDistance == null) {
            PlayerUtils.playerNotFoundError(player.getGameProfile().getName());
            if (resetOnFailure) resetToLocalPlayer();
        } else if (!withinRenderDistance) {
            PlayerUtils.playerOutOfRenderDistanceWarn(player.getGameProfile().getName());
            if (resetOnFailure) resetToLocalPlayer();
        } else {
            target = new TrackedPlayer(player);
            LOGGER.debug("Local Target set: {}", player.getGameProfile().getName());
        }
    }

    public static void resolveRemote(PlayerInfo onlinePlayer, boolean resetOnFailure) {
        if (onlinePlayer == null) return;

        Boolean withinRenderDistance = PlayerUtils.isWithinRenderDistance(onlinePlayer.getProfile().getId());

        if (withinRenderDistance == null) {
            PlayerUtils.playerNotFoundError(onlinePlayer.getProfile().getName());
            if (resetOnFailure) resetToLocalPlayer();
        } else if (!withinRenderDistance && !isServerModded()) {
            PlayerUtils.playerOutOfRenderDistanceWarn(onlinePlayer.getProfile().getName());
            if (resetOnFailure) resetToLocalPlayer();
        } else {
            target = new TrackedPlayer(onlinePlayer);
            LOGGER.debug("Remote Target set: {}", onlinePlayer.getProfile().getName());
        }
    }

    public static void resolveAuto(GameProfile gameProfile, boolean resetOnFailure) {
        if (gameProfile == null) return;

        Boolean withinRenderDistance = PlayerUtils.isWithinRenderDistance(gameProfile.getId());

        if (withinRenderDistance == null) {
            PlayerUtils.playerNotFoundError(gameProfile.getName());
            if (resetOnFailure) resetToLocalPlayer();
        } else if (withinRenderDistance) {
            resolveLocal(PlayerUtils.getPlayerEntity(gameProfile.getId()), resetOnFailure);
        } else {
            resolveRemote(PlayerUtils.getOnlinePlayer(gameProfile.getId()), resetOnFailure);
        }
    }

    public static void onClientJoin() {
        resetToLocalPlayer();
        gameTicks = 0L;
    }

    public static void onResourceManagerReload() {
        int[] scaledSize0 = Utils.scaleTextureToFit(compassTexture, MAX_COMPASS_TEXTURE_SIDE_SIZE);
        compassSize[0] = scaledSize0[0]; compassSize[1] = scaledSize0[1];

        int[] scaledSize1 = Utils.scaleTextureToFit(arrowTexture != null ? arrowTexture : ARROW_UP, MAX_ARROW_TEXTURE_SIDE_SIZE);
        arrowSize[0] = scaledSize1[0]; arrowSize[1] = scaledSize1[1];

        arrowPos[0] = compassPos[0] + (compassSize[0] - arrowSize[0]) / 2;
    }

    public static boolean isCompassHidden() {
        return compassHidden;
    }

    public static void setCompassHidden(boolean hidden) {
        if (compassHidden == hidden) return;

        compassHidden = hidden;
        if (!hidden && target != null) resolveAuto(target.getGameProfile(), true);
    }

    // Private helper methods

    private static void resetToLocalPlayer() {
        target = client().player != null ? new TrackedPlayer(client().player) : null;
    }

    private static void ensureTrackable() {
        if (PlayerUtils.isTrackable(target.getGameProfile().getId()) && clientAndTargetDimensionsMatch()) return;

        Boolean withinRenderDistance = PlayerUtils.isWithinRenderDistance(target.getGameProfile().getId());
        if (!clientAndTargetDimensionsMatch()) {
            PlayerUtils.playerInAnotherDimensionWarn(target.getGameProfile().getName());
        } else if (withinRenderDistance == null) {
            PlayerUtils.playerNotFoundError(target.getGameProfile().getName());
        } else if (!withinRenderDistance) {
            PlayerUtils.playerOutOfRenderDistanceWarn(target.getGameProfile().getName());
        }
        resetToLocalPlayer();
    }

    private static boolean clientAndTargetDimensionsMatch() {
        return client().player != null && client().level != null && target != null
            && (target.getDimension() == null || client().level.dimension().location().equals(target.getDimension().location()));
    }

    private static void recalculateLabelPosition() {
        Font font = Utils.getFont();
        labelPos[0] = cachedGuiWidth / 2 - font.width(trackingLabel) / 2;
        labelPos[1] = cachedGuiHeight - 68 - font.lineHeight / 2;
    }

    private static boolean isSelfTracking() {
        return client().player != null && target != null
            && target.getGameProfile().getId().equals(client().player.getUUID());
    }

    private static boolean isClientInsideTargetHorizontally() {
        if (client().player == null || target == null) return false;

        double localX = client().player.getX(), targetX = target.getX();
        double localZ = client().player.getZ(), targetZ = target.getZ();

        return localX >= targetX - 0.2 && localX <= targetX + 0.2
            && localZ >= targetZ - 0.2 && localZ <= targetZ + 0.2;
    }

    private static int extractAppropriateCompassFrame(double minecraftAngle) {
        return (int) ((minecraftAngle + HALF_FRAME_ANGLE_DEG) / FRAME_ANGLE_DEG) % 32;
    }

    private static ResourceLocation currentTexture() {
        return compassFrame != COMPASS_STATIC_FRAME
            ?
            ResourceLocation.withDefaultNamespace(
                "textures/item/compass_" + (compassFrame < 10 ? "0" + compassFrame : compassFrame) + ".png"
            )
            : COMPASS_STATIC;
    }
}
