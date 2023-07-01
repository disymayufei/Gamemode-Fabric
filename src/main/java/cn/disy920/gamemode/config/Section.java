package cn.disy920.gamemode.config;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Section {
    protected final JsonObject configObj;
    protected final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected Section(@NotNull JsonObject configObj) {
        this.configObj = configObj;
    }

    @NotNull
    public Section getSection() {
        return this;
    }

    @Nullable
    public Section getSection(@NotNull String path) {
        JsonElement nextObj = configObj.get(path);

        if (nextObj == null || !nextObj.isJsonObject()) {
            return null;
        }

        return new Section(nextObj.getAsJsonObject());
    }

    public long getLong(@NotNull String path) {
        return getLong(path, 0L);
    }

    public long getLong(@NotNull String path, long def) {
        JsonElement ele = configObj.get(path);

        if (ele != null) {
            if (ele instanceof JsonPrimitive primitive) {
                try {
                    Number number = primitive.getAsNumber();

                    return number.longValue();
                } catch (Exception e) {
                    return def;
                }
            }
        }

        return def;
    }

    public int getInt(@NotNull String path) {
        return getInt(path, 0);
    }

    public int getInt(@NotNull String path, int def) {
        JsonElement ele = configObj.get(path);

        if (ele != null) {
            if (ele instanceof JsonPrimitive primitive) {
                try {
                    Number number = primitive.getAsNumber();

                    return number.intValue();
                }
                catch (Exception e) {
                    return def;
                }
            }

        }

        return def;
    }

    public float getFloat(@NotNull String path) {
        return getFloat(path, 0.0F);
    }

    public float getFloat(@NotNull String path, float def) {
        JsonElement ele = configObj.get(path);

        if (ele != null) {
            if (ele instanceof JsonPrimitive primitive) {
                try {
                    Number number = primitive.getAsNumber();

                    return number.floatValue();
                }
                catch (Exception e) {
                    return def;
                }
            }

        }

        return def;
    }

    public double getDouble(@NotNull String path) {
        return getDouble(path, 0.0);
    }

    public double getDouble(@NotNull String path, double def) {
        JsonElement ele = configObj.get(path);

        if (ele != null) {
            if (ele instanceof JsonPrimitive primitive) {
                try {
                    Number number = primitive.getAsNumber();

                    return number.doubleValue();
                }
                catch (Exception e) {
                    return def;
                }
            }

        }

        return def;
    }

    public boolean getBoolean(String path) {
        return getBoolean(path,false);
    }

    public boolean getBoolean(String path, boolean def) {
        JsonElement ele = configObj.get(path);

        if (ele != null) {
            if (ele instanceof JsonPrimitive primitive) {
                try {
                    return primitive.getAsBoolean();
                }
                catch (Exception e) {
                    return def;
                }
            }

        }

        return def;
    }

    @Nullable
    public String getString(@NotNull String path) {
        return getString(path, null);
    }

    public String getString(@NotNull String path, String def) {
        JsonElement ele = configObj.get(path);

        if (ele != null) {
            if (ele instanceof JsonPrimitive primitive) {
                return primitive.getAsString();
            }
        }

        return def;
    }

    @NotNull
    public List<String> getStringList(String path) {
        List<String> result = new ArrayList<>();

        JsonElement ele = configObj.get(path);
        if (ele != null) {
            if (ele instanceof JsonArray array) {
                for (JsonElement element : array) {
                    result.add(element.getAsString());
                }
            }
        }

        return result;
    }

    public void set(@NotNull String path, @Nullable Object obj) {
        if (obj == null) {
            configObj.add(path, JsonNull.INSTANCE);
        }
        else if (obj instanceof String str) {
            configObj.addProperty(path, str);
        }
        else if (obj instanceof Number number) {
            configObj.addProperty(path, number);
        }
        else if (obj instanceof Character character) {
            configObj.addProperty(path, character);
        }
        else if (obj instanceof Boolean bool) {
            configObj.addProperty(path, bool);
        }
        else if (obj instanceof JsonElement element) {
            configObj.add(path, element);
        }
        else {
            if (obj instanceof List<?> list) {
                JsonArray array = GSON.fromJson(GSON.toJson(list), JsonArray.class);
                configObj.add(path, array);
            }
            else {
                JsonObject jsonObject = GSON.fromJson(GSON.toJson(obj), JsonObject.class);
                configObj.add(path, jsonObject);
            }
        }
    }
}
