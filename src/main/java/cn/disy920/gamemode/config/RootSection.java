package cn.disy920.gamemode.config;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RootSection extends Section {
    public RootSection(JsonObject configObj) {
        super(configObj);
    }

    public void save(File file) throws IOException {
        Files.writeString(file.toPath(), GSON.toJson(configObj));
    }
}
