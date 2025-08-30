package me.chorus.simpletrackingcompass.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class PlayerUtils {
    public static void playerNotFoundError(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.getName().getString().equals(playerName)) return;

        Text message =
                Text.literal(
                                playerName
                        ).formatted(Formatting.YELLOW)

                        .append(
                                Text.literal(
                                        " cannot be found. Target Player was set back to:\n"
                                ).formatted(Formatting.RED)
                        )

                        .append(
                                Text.literal(
                                        client.player.getName().getString()
                                ).formatted(Formatting.YELLOW)
                        );

        client.player.sendMessage(message, false);
    }

    public static void playerOutOfRenderDistanceWarn(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.getName().getString().equals(playerName)) return;

        Text message =
                Text.literal(
                                playerName
                        ).formatted(Formatting.YELLOW)

                        .append(
                                Text.literal(
                                        " is not within your render distance."
                                ).formatted(Formatting.GOLD)
                        );

        client.player.sendMessage(message, false);
    }

    public static void playerInAnotherDimensionWarn(String playerName, Identifier dim) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.getName().getString().equals(playerName) || dim == null) return;

        Text message =
                Text.literal(
                                playerName
                        ).formatted(Formatting.YELLOW)

                        .append(
                                Text.literal(
                                        String.format(" is in another dimension (%s).", dim)
                                ).formatted(Formatting.GOLD)
                        );

        client.player.sendMessage(message, false);
    }

    // Returns null if the player is not on the server
    public static Boolean isWithinRenderDistance(UUID uuid) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (uuid == null || client == null || client.world == null ||
                client.getNetworkHandler() == null) return null;

        boolean listedInTab = false;
        for (PlayerListEntry p : client.getNetworkHandler().getPlayerList()) {
            if (p.getProfile().getId().equals(uuid)) {
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
}