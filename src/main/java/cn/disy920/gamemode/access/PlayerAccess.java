package cn.disy920.gamemode.access;

import cn.disy920.gamemode.utils.PlayerPosition;
import net.minecraft.world.World;

public interface PlayerAccess {
    boolean isSpectating();

    void setSpectating(boolean spectating);

    PlayerPosition getPreviousPosition();

    void setPreviousPosition(World world, double x, double y, double z, float yaw, float pitch);
}
