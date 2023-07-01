package cn.disy920.gamemode.utils;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import static cn.disy920.gamemode.Main.server;

public class PlayerPosition {
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public PlayerPosition() {}

    public PlayerPosition(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world.getRegistryKey().getValue().toString();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public PlayerPosition(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public ServerWorld takeServerWorld() {
        for (ServerWorld world1 : server.getWorlds()) {
            if (world1.getRegistryKey().getValue().toString().equals(world)) {
                return world1;
            }
        }

        return null;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
