package cn.disy920.gamemode.mixin;

import cn.disy920.gamemode.access.PlayerAccess;
import cn.disy920.gamemode.config.JsonConfig;
import cn.disy920.gamemode.config.RootSection;
import cn.disy920.gamemode.config.Section;
import cn.disy920.gamemode.utils.PlayerPosition;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static cn.disy920.gamemode.Main.globalConfig;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements PlayerAccess {

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract void sendMessage(Text message);

    @Shadow @Final private static Logger LOGGER;
    private boolean spectating = false;
    private PlayerPosition previousPosition = null;
    private RootSection config = null;
    private File configFile = null;

    private final String name = getName().getString();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectInit(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
        File dataDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "Gamemode/playerData" );
        if (!dataDir.exists() || dataDir.isFile()) {
            dataDir.mkdirs();
        }

        File oldDataFile = new File(dataDir, name + ".json");
        File dataFile = new File(dataDir, uuid + ".json");

        try {
            if (oldDataFile.exists()) {  // 转换旧的数据文件
                RootSection temp = JsonConfig.loadConfig(oldDataFile);
                boolean spectating = temp.getBoolean("spectating");
                Section section = temp.getSection("prevPosition");

                config = JsonConfig.loadConfig(dataFile);
                config.set("spectating", spectating);
                config.set("prevPosition", section);

                config.save(configFile);

                oldDataFile.delete();

                return;
            }

            if (!dataFile.exists()) {
                dataFile.createNewFile();
                Files.writeString(dataFile.toPath(), "{\"spectating\": false, \n\"prevPosition\": null}");
            }

            configFile = dataFile;
            config = JsonConfig.loadConfig(dataFile);
            this.spectating = config.getBoolean("spectating");

            Section prev = config.getSection("prevPosition");

            if (prev != null) {
                this.previousPosition = new PlayerPosition(
                        prev.getString("world"),
                        prev.getDouble("x"),
                        prev.getDouble("y"),
                        prev.getDouble("z"),
                        prev.getFloat("yaw"),
                        prev.getFloat("pitch")
                );
            }
        }
        catch (Exception e) {
            LOGGER.error("创建玩家" + name + "的配置文件时发生错误，以下是错误的堆栈信息:");
            e.printStackTrace();
            ((ServerPlayerEntity)(Object)this).networkHandler.disconnect(Text.literal("初始您的观察者模式失败，请联系服务器管理员查看后台报错").setStyle(Style.EMPTY.withColor(Formatting.RED)));
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    public void injectTeleport$1(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, CallbackInfoReturnable<Boolean> cir) {
        if (isSpectating()) {
            PlayerEntity player = world.getClosestPlayer(destX, destY, destZ, 2, false);
            List<String> blacklist = globalConfig.getStringList("deny-tp-to-player");

            if (player != null) {
                String playerName = player.getName().getString();
                for (String name : blacklist) {
                    if (playerName.equalsIgnoreCase(name)) {
                        this.sendMessage(Text.literal("该玩家不允许您在观察者模式下传送到TA身旁").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At("HEAD"), cancellable = true)
    public void injectTeleport$2(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (isSpectating()) {
            PlayerEntity player = targetWorld.getClosestPlayer(x, y, z, 2, false);
            List<String> blacklist = globalConfig.getStringList("deny-tp-to-player");

            if (player != null) {
                String playerName = player.getName().getString();
                for (String name : blacklist) {
                    if (playerName.equalsIgnoreCase(name)) {
                        this.sendMessage(Text.literal("该玩家不允许您在观察者模式下传送到TA身旁").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Override
    public boolean isSpectating() {
        return this.spectating;
    }

    @Override
    public void setSpectating(boolean spectating) {
        this.spectating = spectating;

        if (this.config != null) {
            config.set("spectating", spectating);
            try {
                config.save(configFile);
            } catch (Exception e) {
                LOGGER.error("写入玩家" + name + "的配置文件时发生错误，以下是错误的堆栈信息:");
                e.printStackTrace();
            }
        }
    }

    @Override
    @Nullable
    public PlayerPosition getPreviousPosition() {
        return this.previousPosition;
    }


    @Override
    public void setPreviousPosition(World world, double x, double y, double z, float yaw, float pitch) {
        this.previousPosition = new PlayerPosition(world, x, y, z, yaw, pitch);

        if (config != null) {
            config.set("prevPosition", previousPosition);
            try {
                config.save(configFile);
            } catch (Exception e) {
                LOGGER.error("写入玩家" + name + "的配置文件时发生错误，以下是错误的堆栈信息:");
                e.printStackTrace();
            }
        }
    }
}
