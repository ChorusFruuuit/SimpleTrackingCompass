package net.chorus.simpletrackingcompass.util;

import com.mojang.authlib.GameProfile;
import net.chorus.simpletrackingcompass.SimpleTrackingCompass;
import net.chorus.simpletrackingcompass.mixin.HudAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;
import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public final class PlayerUtils {
    public static void playerNotFoundError(String name) {
        if (client().player == null || client().player.getPlainTextName().equals(name)) return;

        MutableComponent message = Component.empty()
            .append(Component.literal("There is no player with the name ")
                .withStyle(ChatFormatting.RED))
            .append(Component.literal(name)
                .withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" currently playing on this server.")
                .withStyle(ChatFormatting.RED));

        setOverlayMessage(message, 120);
    }

    public static void playerOutOfRenderDistanceWarn(String name) {
        if (client().player == null || client().player.getPlainTextName().equals(name)) return;

        MutableComponent message = Component.empty()
            .append(Component.literal(name)
                .withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" is not within your render distance.")
                .withStyle(ChatFormatting.GOLD));

        setOverlayMessage(message, 120);
    }

    public static void playerInAnotherDimensionWarn(String name) {
        if (client().player == null || client().player.getPlainTextName().equals(name)) return;

        MutableComponent message = Component.empty()
            .append(Component.literal("Yours and ")
                .withStyle(ChatFormatting.GOLD))
            .append(Component.literal(name + "'s")
                .withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" dimensions don't match.")
                .withStyle(ChatFormatting.GOLD));

        setOverlayMessage(message, 120);
    }

    /**
     * Returns the following guide message:
     * <p>
     *     Welcome to Simple Tracking Compass mod.<br>
     *     Click Here to get started or use the ESC &rarr; Compass Button.
     * </p>
     * <p>Client Commands:</p>
     * <ul>
     *     <li>{@code /selector} - Opens the Target Selector.</li>
     *     <li>{@code /STCGuide} - This guide message.</li>
     *     <li>{@code /track <player>} - Sets the given player as a target.</li>
     *     <li>{@code /trackNearest} - Sets the nearest player as a target.</li>
     * </ul>
     * */
    public static Component STCGuideMessage() {
        return Component.empty()
            .append(Component.literal("Welcome to Simple Tracking Compass mod.\nClick ")
                .withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Here")
                .withStyle(style -> style
                    .withColor(ChatFormatting.AQUA)
                    .withBold(true)
                    .withClickEvent(new ClickEvent.RunCommand("selector"))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to open Target Selector")))
                ))
            .append(Component.literal(" to get started or use the ")
                .withStyle(ChatFormatting.BLUE))
            .append(Component.literal("ESC → Compass Button\n\n")
                .withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Client Commands:\n")
                .withStyle(ChatFormatting.GOLD))
            .append(Component.literal("• /selector")
                .withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" - Opens the Target Selector.\n")
                .withStyle(ChatFormatting.GOLD))
            .append(Component.literal("• /STCGuide")
                .withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" - This guide message.\n")
                .withStyle(ChatFormatting.GOLD))
            .append(Component.literal("• /track <player>")
                .withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" - Sets the given player as a target.\n")
                .withStyle(ChatFormatting.GOLD))
            .append(Component.literal("• /trackNearest")
                .withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" - Sets the nearest player as a target.")
                .withStyle(ChatFormatting.GOLD));
    }

    public static List<AbstractClientPlayer> clientPlayers() {
        return client().level != null ? client().level.players() : List.of();
    }

    public static Collection<PlayerInfo> onlinePlayers() {
        return client().getConnection() != null ? client().getConnection().getOnlinePlayers() : List.of();
    }

    public static Player getPlayerEntity(UUID uuid) {
        if (uuid == null || client().level == null) return null;

        return client().level.getEntity(uuid) instanceof Player player ? player : null;
    }

    public static PlayerInfo getOnlinePlayer(UUID uuid) {
        if (uuid == null || client().getConnection() == null) return null;

        return client().getConnection().getPlayerInfo(uuid);
    }

    @SuppressWarnings("resource")
    public static <P extends Player> P getNearestPlayer(P centerPlayer, List<P> playerList) {
        if (centerPlayer == null || playerList.isEmpty()) return null;

        P nearest = null;
        for (P player : playerList) {
            if (player.getUUID().equals(centerPlayer.getUUID()) || !player.level().dimension().identifier().equals(centerPlayer.level().dimension().identifier()))
                continue;

            if (nearest == null || centerPlayer.distanceToSqr(player) < centerPlayer.distanceToSqr(nearest)) {
                nearest = player;
            }
        }

        return nearest;
    }

    public static boolean isValidOnlinePlayer(GameProfile gameProfile) {
        return getOnlinePlayer(gameProfile.id()) != null
            && !gameProfile.name().isEmpty()
            // Standard minecraft name regex (no length matching)
            && gameProfile.name().matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Returns {@code null} if there isn't a player with such {@code uuid} on the server
     */
    public static Boolean isWithinRenderDistance(UUID uuid) {
        return getOnlinePlayer(uuid) != null ? getPlayerEntity(uuid) != null : null;
    }

    /**
     * Doesn't account for the entity (whose {@link UUID} is being passed as a parameter) being in another dimension
     * (unless {@link SimpleTrackingCompass#isServerModded() isServerModded()} is false)
     * */
    public static boolean isTrackable(UUID uuid) {
        Boolean withinRenderDistance = isWithinRenderDistance(uuid);
        return withinRenderDistance != null && (isServerModded() || withinRenderDistance);
    }

    public static boolean inWorld() {
        return client().level != null && client().player != null;
    }

    public static void setOverlayMessage(Component message, int ticks) {
        HudAccessor hudAccessor = Utils.getHudAccessor();
        hudAccessor.setOverlayMessageString(message);
        hudAccessor.setOverlayMessageTime(ticks);
    }
}
