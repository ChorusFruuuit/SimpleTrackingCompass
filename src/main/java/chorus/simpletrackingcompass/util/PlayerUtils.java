package chorus.simpletrackingcompass.util;

import chorus.simpletrackingcompass.mixin.InGameHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class PlayerUtils {
    public static void playerNotFoundError(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.getName().getString().equals(playerName)) return;

        MutableText message =
            Text.literal(
                    playerName
                ).formatted(Formatting.YELLOW)

                .append(
                    Text.literal(
                        " cannot be found. Target Player was set back to:"
                    ).formatted(Formatting.RED)
                )

                .append(
                    Text.literal(
                        client.player.getName().getString()
                    ).formatted(Formatting.YELLOW)
                );

        setOverlayMessage(message, false, 120);
    }

    public static void playerOutOfRenderDistanceWarn(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.getName().getString().equals(playerName)) return;

        MutableText message =
            Text.literal(
                    playerName
                ).formatted(Formatting.YELLOW)

                .append(
                    Text.literal(
                        " is not within your render distance."
                    ).formatted(Formatting.GOLD)
                );

        setOverlayMessage(message, false, 120);
    }

    public static void playerInAnotherDimensionWarn(String playerName, Identifier dim) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.getName().getString().equals(playerName) || dim == null)
            return;

        MutableText message =
            Text.literal(
                    playerName
                ).formatted(Formatting.YELLOW)

                .append(
                    Text.literal(
                        String.format(" is in another dimension (%s).", dim)
                    ).formatted(Formatting.GOLD)
                );

        setOverlayMessage(message, false, 120);
    }

    public static void guideMessage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        MutableText message =
            Text.literal(
                    "To select a tracking target, click "
                ).formatted(Formatting.BLUE)

                .append(
                    Text.literal("Here")
                        .styled(style -> style
                            .withColor(0x55FFFF) // aqua
                            .withBold(true)
                            .withClickEvent(
                                new ClickEvent.RunCommand(
                                    "selector"
                                )
                            )
                            .withHoverEvent(
                                new HoverEvent.ShowText(
                                    Text.literal(
                                        "Click to open Target Selector"
                                    )
                                )
                            )
                        )
                )
                .append(Text.literal(" or open the selector manually via")
                    .formatted(Formatting.BLUE))
                .append(Text.literal("\nESC → Compass Button")
                    .formatted(Formatting.YELLOW));

        client.player.sendMessage(message, false);
    }

    /**
     * Returns null if the player is not on the server
     */
    public static Boolean isWithinRenderDistance(UUID uuid) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (uuid == null || client == null || client.world == null ||
            client.getNetworkHandler() == null) return null;

        boolean listedInTab = false;
        for (PlayerListEntry p : client.getNetworkHandler().getPlayerList()) {
            if (p.getProfile().id().equals(uuid)) {
                listedInTab = true;
                break;
            }
        }

        boolean withinRenderDistance = false;
        for (PlayerEntity p : client.world.getPlayers()) {
            if (p.getUuid().equals(uuid)) {
                withinRenderDistance = true;
                break;
            }
        }

        return listedInTab ? withinRenderDistance : null;
    }

    public static PlayerEntity getPlayerEntity(UUID uuid) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return null;

        PlayerEntity player = null;
        for (PlayerEntity p : client.world.getPlayers()) {
            if (uuid.equals(p.getUuid())) {
                player = p;
                break;
            }
        }

        return player;
    }

    public static boolean inWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world != null && client.player != null;
    }

    public static void setOverlayMessage(Text message, boolean tinted, int ticks) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.inGameHud.setOverlayMessage(message, tinted);

        InGameHudAccessor inGameHud = (InGameHudAccessor) client.inGameHud;
        inGameHud.setOverlayRemaining(ticks);
    }
}