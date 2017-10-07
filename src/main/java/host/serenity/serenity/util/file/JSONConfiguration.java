package host.serenity.serenity.util.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import host.serenity.serenity.api.file.FileManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JSONConfiguration {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private File file;

    public JSONConfiguration(String fileName) {
        this.file = FileManager.createClientFile(fileName);
        this.file.getParentFile().mkdirs();
    }

    public <T> T load(Class<T> type) {
        try {
            JsonReader reader = new JsonReader(new StringReader( new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8)));
            reader.setLenient(true);
            T loaded = GSON.fromJson(reader, type);
            if (loaded == null) {
                try {
                    return type.newInstance();
                } catch (InstantiationException | IllegalAccessException ignored) {
                }
            }
            return loaded;
        } catch (Exception ignored) {
        }
        return null;
    }

    public <T> void save(T toSave) {
        try {
            if (!this.file.exists()) {
                this.file.createNewFile();
            }
            Writer writer = new FileWriter(this.file);
            GSON.toJson(toSave, writer);
            writer.close();
        } catch (IOException ignored) {
        }
    }

    public void delete() {
        this.file.delete();
    }
}
