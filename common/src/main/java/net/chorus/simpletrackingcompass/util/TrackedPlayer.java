package net.chorus.simpletrackingcompass.util;

import com.mojang.authlib.GameProfile;
import net.chorus.simpletrackingcompass.network.ClientNetworking;
import net.chorus.simpletrackingcompass.network.DestinationStorage;
import net.chorus.simpletrackingcompass.network.PacketDestination;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;

public class TrackedPlayer implements PacketDestination {
    /** {@code null} when TrackedPlayer is remote */
    private final Player player;
    private final GameProfile gameProfile;

    private Vec3 position = new Vec3(0, 0, 0);
    private ResourceKey<Level> dimension = null;

    public TrackedPlayer(@NotNull Player player) {
        this.player = player;
        this.gameProfile = player.getGameProfile();

        DestinationStorage.remove(TRACKED_PLAYER);
        DestinationStorage.put(TRACKED_PLAYER, this);

        this.update();
    }

    public TrackedPlayer(@NotNull PlayerInfo onlinePlayer) {
        this.player = null;
        this.gameProfile = onlinePlayer.getProfile();

        DestinationStorage.remove(TRACKED_PLAYER);
        DestinationStorage.put(TRACKED_PLAYER, this);

        this.update();
    }

    public Player getPlayer() {
        return player;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public double getX() {
        return position != null ? position.x() : 0;
    }

    public double getY() {
        return position != null ? position.y() : 0;
    }

    public double getZ() {
        return position != null ? position.z() : 0;
    }

    @SuppressWarnings("resource")
    public void update() {
        if (player != null) {
            setData(player.position(), player.level().dimension());
        } else {
            ClientNetworking.sendServerboundPacket(new PlayerPositionRequest(
                Optional.ofNullable(gameProfile.getId()),
                TRACKED_PLAYER
            ));
        }
    }

    private void setData(Vec3 position, ResourceKey<Level> dimension) {
        this.position = position;
        this.dimension = dimension;
    }

    // Called by ClientNetworking when receiving a packet payload

    @Override
    public void processPacket(CustomPacketPayload packetPayload) {
        if (!(packetPayload instanceof PlayerPositionResponse positionResponse)
            || positionResponse.playerPosition().isEmpty()
            || positionResponse.playerDimension().isEmpty()) return;

        setData(positionResponse.playerPosition().get(), positionResponse.playerDimension().get());
    }
}
