package me.chorus.simpletrackingcompass.util;

import me.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class TrackedPlayer {
    private final PlayerEntity entity; // if null -- TrackedPlayer is remote

    private final UUID uuid;
    private final String name;

    private Identifier dimension = null;
    private double x = 0;
    private double z = 0;

    private boolean wasPositionChanged = false;

    public TrackedPlayer(PlayerEntity player) {
        this.entity = player;

        this.uuid = player.getUuid();
        this.name = player.getName().getString();

        this.update();
    }

    public TrackedPlayer(UUID uuid, String name) {
        this.entity = null;

        this.uuid = uuid;
        this.name = name;

        this.update();
    }

    public PlayerEntity getEntity() {return entity;}

    public UUID getUuid() {return uuid;}

    public String getName() {return name;}

    public Identifier getDimension() {return dimension;}

    public double getX() {return x;}

    public double getZ() {return z;}

    public boolean hasPositionBeenEverChanged() {return wasPositionChanged;}

    public void update() {
        if (entity != null) {
            dimension = entity.getWorld().getRegistryKey().getValue();
            x = entity.getX();
            z = entity.getZ();

            if (!wasPositionChanged) wasPositionChanged = true;
        }
        else {
            ClientPlayNetworking.send(new PlayerPositionRequest(uuid));
        }
    }

    public void setRemoteData(Identifier dim, Vec3d pos) {
        this.dimension = dim;
        this.x = pos.x;
        this.z = pos.z;

        if (!wasPositionChanged) wasPositionChanged = true;
    }
}
