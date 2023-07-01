package cn.disy920.gamemode;

import cn.disy920.gamemode.command.SpecCommand;
import cn.disy920.gamemode.config.JsonConfig;
import cn.disy920.gamemode.config.RootSection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main implements ModInitializer {

    public static Logger LOGGER = LoggerFactory.getLogger("StarLight-Ore Billboard");
    public static RootSection config = null;
    public static MinecraftServer server = null;

    public void onEnable() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SpecCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitialize() {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "Gamemode");
        if (!configDir.exists() || configDir.isFile()) {
            configDir.mkdirs();
        }

        File configFile = new File(configDir, "config.json");

        JsonConfig.saveDefaultConfig(configFile.toPath());
        try {
            config = JsonConfig.loadConfig(configFile);
        }
        catch (Exception e) {
            LOGGER.error("配置文件加载失败，本Mod无法运行！错误的堆栈信息如下：");
            e.printStackTrace();
            return;
        }

        ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer) -> server = minecraftServer);

        onEnable();
    }
}
