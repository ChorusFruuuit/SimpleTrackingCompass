package net.chorus.simpletrackingcompass.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.chorus.simpletrackingcompass.CompassTracker;
import net.chorus.simpletrackingcompass.network.ClientNetworking;
import net.chorus.simpletrackingcompass.network.DestinationStorage;
import net.chorus.simpletrackingcompass.network.PacketDestination;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.chorus.simpletrackingcompass.screen.TargetSelectorScreen;
import net.chorus.simpletrackingcompass.util.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;
import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public final class ClientCommands implements PacketDestination {
    public static <S extends SharedSuggestionProvider> void register(CommandDispatcher<S> dispatcher) {
        registerSelectorCommand(dispatcher);
        registerSTCGuideCommand(dispatcher);
        registerTrackCommand(dispatcher);
        registerTrackNearestCommand(dispatcher);

        DestinationStorage.remove(CLIENT_COMMANDS);
        DestinationStorage.put(CLIENT_COMMANDS, new ClientCommands());
    }

    private static <S extends SharedSuggestionProvider> void registerSelectorCommand(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("selector")
            .executes(_ -> {
                client().schedule(() -> client().gui.setScreen(new TargetSelectorScreen(null)));
                return Command.SINGLE_SUCCESS;
            })
        );
    }

    private static <S extends SharedSuggestionProvider> void registerSTCGuideCommand(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("STCGuide")
            .executes(_ -> {
                client().gui.hud.getChat().addClientSystemMessage(PlayerUtils.STCGuideMessage());
                return Command.SINGLE_SUCCESS;
            })
        );
    }

    private static <S extends SharedSuggestionProvider> void registerTrackCommand(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("track")
            .then(RequiredArgumentBuilder.<S, String>argument("player", StringArgumentType.word())
                .suggests((context, builder) -> {
                    String remaining = builder.getRemainingLowerCase();

                    for (String playerName : context.getSource().getOnlinePlayerNames()) {
                        if (playerName.toLowerCase().contains(remaining)) {
                            builder.suggest(playerName);
                        }
                    }

                    return builder.buildFuture();
                })
                .executes(context -> {
                    if (client().getConnection() == null) return 0;

                    String targetName = StringArgumentType.getString(context, "player");

                    for (PlayerInfo onlinePlayer : PlayerUtils.onlinePlayers()) {
                        if (onlinePlayer.getProfile().name().equalsIgnoreCase(targetName)) {
                            CompassTracker.resolveAuto(onlinePlayer.getProfile(), false);
                            return Command.SINGLE_SUCCESS;
                        }
                    }

                    client().gui.hud.getChat().addClientSystemMessage(Component.literal(
                        "Could not find a player with the name " + targetName
                    ).withStyle(ChatFormatting.RED));

                    return 0;
                })
            )
        );
    }

    private static <S extends SharedSuggestionProvider> void registerTrackNearestCommand(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("trackNearest")
            .executes(_ -> {
                LocalPlayer local = client().player;
                if (local == null || client().level == null || client().getConnection() == null) return 0;

                Collection<PlayerInfo> onlinePlayers = PlayerUtils.onlinePlayers();
                List<AbstractClientPlayer> validClientPlayers = PlayerUtils.clientPlayers().stream()
                    .filter(player -> PlayerUtils.isValidOnlinePlayer(player.getGameProfile()))
                    .toList();

                if (onlinePlayers.isEmpty() || validClientPlayers.isEmpty()) {
                    // Safety net; should be unreachable
                    return 0;
                }

                if (onlinePlayers.size() == 1) {
                    if (onlinePlayers.iterator().next().getProfile().id().equals(local.getUUID())) {
                        client().gui.hud.getChat().addClientSystemMessage(Component.literal(
                            "You're the only player currently online!"
                        ));
                        return Command.SINGLE_SUCCESS;
                    } else {
                        // Safety net; should be unreachable
                        return 0;
                    }
                }

                AbstractClientPlayer nearest = PlayerUtils.getNearestPlayer(local, validClientPlayers);

                if (nearest == null) {
                    if (!isServerModded()) {
                        client().gui.hud.getChat().addClientSystemMessage(Component.literal(
                            "Could not find a player within your render distance"
                        ).withStyle(ChatFormatting.RED));
                    } else {
                        ClientNetworking.sendServerboundPacket(new PlayerPositionRequest(
                            Optional.empty(),
                            CLIENT_COMMANDS
                        ));
                    }
                } else {
                    CompassTracker.resolveLocal(nearest, false);
                }

                return Command.SINGLE_SUCCESS;
            })
        );
    }

    // Called by ClientNetworking when receiving a packet payload

    @Override
    public void processPacket(CustomPacketPayload packetPayload) {
        if (!(packetPayload instanceof PlayerPositionResponse positionResponse)) return;

        if (positionResponse.playerUUID().isEmpty()
            || positionResponse.playerPosition().isEmpty()
            || positionResponse.playerDimension().isEmpty()) {
            client().gui.hud.getChat().addClientSystemMessage(Component.literal(
                "You're the only player in this dimension!"
            ));
            return;
        }

        PlayerInfo onlinePlayer = PlayerUtils.getOnlinePlayer(positionResponse.playerUUID().get());
        if (onlinePlayer == null) return;

        CompassTracker.resolveAuto(onlinePlayer.getProfile(), false);
    }
}
