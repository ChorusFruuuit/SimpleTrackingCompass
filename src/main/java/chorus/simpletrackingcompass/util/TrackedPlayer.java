package chorus.simpletrackingcompass.util;

import chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public class TrackedPlayer {
    private final Player entity; // if null -- TrackedPlayer is remote

    private final UUID uuid;
    private final String name;

    private Identifier dimension = null;
    private double x = 0;
    private double z = 0;

    public TrackedPlayer(Player player) {
        this.entity = player;

        this.uuid = player.getUUID();
        this.name = player.getName().getString();

        this.update();
    }

    public TrackedPlayer(UUID uuid, String name) {
        this.entity = null;

        this.uuid = uuid;
        this.name = name;

        this.update();
    }

    public Player getEntity() {
        return entity;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Identifier getDimension() {
        return dimension;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    @SuppressWarnings("resource")
    public void update() {
        if (entity != null) {
            dimension = entity.level().dimension().identifier();
            x = entity.getX();
            z = entity.getZ();
        } else {
            ClientPlayNetworking.send(new PlayerPositionRequest(uuid));
        }
    }

    public void setRemoteData(Identifier dim, Vec3 pos) {
        this.dimension = dim;
        this.x = pos.x;
        this.z = pos.z;
    }
}
