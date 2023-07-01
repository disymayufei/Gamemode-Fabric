package cn.disy920.gamemode.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonConfig {
    protected static final Gson GSON = new GsonBuilder().setLenient().create();

    public static RootSection loadConfig(File file) throws IOException {
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException("Couldn't not found the config file!");
        }
        return new RootSection(GSON.fromJson(new FileReader(file), JsonObject.class));
    }

    public static void saveDefaultConfig(Path configPath) {
        try (InputStream resource = JsonConfig.class.getResourceAsStream("/config.json")) {
            if (resource != null) {
                File configFile = configPath.toFile();

                if (!configFile.exists() || configFile.isDirectory()) {
                    configFile.createNewFile();
                    Files.writeString(configPath, new String(resource.readAllBytes()));
                }
            }
        }
        catch (IOException ignored) {

        }
    }
}
