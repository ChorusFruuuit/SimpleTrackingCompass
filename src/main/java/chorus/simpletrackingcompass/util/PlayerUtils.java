package chorus.simpletrackingcompass.util;

import chorus.simpletrackingcompass.mixin.InGameHudAccessor;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class PlayerUtils {
    public static void playerNotFoundError(String playerName) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.getName().getString().equals(playerName)) return;

        MutableComponent message =
            Component.literal(
                    playerName
                ).withStyle(ChatFormatting.YELLOW)

                .append(
                    Component.literal(
                        " cannot be found. Target Player was set back to: "
                    ).withStyle(ChatFormatting.RED)
                )

                .append(
                    Component.literal(
                        client.player.getName().getString()
                    ).withStyle(ChatFormatting.YELLOW)
                );

        setOverlayMessage(message, false, 120);
    }

    public static void playerOutOfRenderDistanceWarn(String playerName) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.getName().getString().equals(playerName)) return;

        MutableComponent message =
            Component.literal(
                    playerName
                ).withStyle(ChatFormatting.YELLOW)

                .append(
                    Component.literal(
                        " is not within your render distance."
                    ).withStyle(ChatFormatting.GOLD)
                );

        setOverlayMessage(message, false, 120);
    }

    public static void playerInAnotherDimensionWarn(String playerName, Identifier dim) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.getName().getString().equals(playerName) || dim == null)
            return;

        MutableComponent message =
            Component.literal(
                    playerName
                ).withStyle(ChatFormatting.YELLOW)

                .append(
                    Component.literal(
                        String.format(" is in another dimension (%s).", dim)
                    ).withStyle(ChatFormatting.GOLD)
                );

        setOverlayMessage(message, false, 120);
    }

    public static void guideMessage() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        MutableComponent message =
            Component.literal(
                    "To select a tracking target, click "
                ).withStyle(ChatFormatting.BLUE)

                .append(
                    Component.literal("Here")
                        .withStyle(style -> style
                            .withColor(0x55FFFF) // aqua
                            .withBold(true)
                            .withClickEvent(
                                new ClickEvent.RunCommand(
                                    "selector"
                                )
                            )
                            .withHoverEvent(
                                new HoverEvent.ShowText(
                                    Component.literal(
                                        "Click to open Target Selector"
                                    )
                                )
                            )
                        )
                )
                .append(Component.literal(" or open the selector manually via")
                    .withStyle(ChatFormatting.BLUE))
                .append(Component.literal("\nESC → Compass Button")
                    .withStyle(ChatFormatting.YELLOW));

        client.player.sendSystemMessage(message);
    }

    /**
     * Returns null if the player is not on the server
     */
    public static Boolean isWithinRenderDistance(UUID uuid) {
        Minecraft client = Minecraft.getInstance();
        if (uuid == null || client.level == null || client.getConnection() == null) return null;

        boolean listedInTab = false;
        for (PlayerInfo p : client.getConnection().getOnlinePlayers()) {
            if (p.getProfile().id().equals(uuid)) {
                listedInTab = true;
                break;
            }
        }

        boolean withinRenderDistance = false;
        for (Player p : client.level.players()) {
            if (p.getUUID().equals(uuid)) {
                withinRenderDistance = true;
                break;
            }
        }

        return listedInTab ? withinRenderDistance : null;
    }

    public static Player getPlayerEntity(UUID uuid) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return null;

        Player player = null;
        for (Player p : client.level.players()) {
            if (uuid.equals(p.getUUID())) {
                player = p;
                break;
            }
        }

        return player;
    }

    public static boolean inWorld() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.player != null;
    }

    public static void setOverlayMessage(Component message, boolean tinted, int ticks) {
        Minecraft client = Minecraft.getInstance();
        client.gui.setOverlayMessage(message, tinted);

        InGameHudAccessor inGameHud = (InGameHudAccessor) client.gui;
        inGameHud.setOverlayRemaining(ticks);
    }
}