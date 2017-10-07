package host.serenity.serenity.api.file.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;
import host.serenity.serenity.api.file.ClientDataHandler;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class AbstractJsonDataHandler<T> implements ClientDataHandler {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Class<T> targetClass;
    private final File file;

    public AbstractJsonDataHandler(Class<T> targetClass, File file) {
        this.targetClass = targetClass;
        this.file = file;
    }

    protected abstract T getObjectToSave();
    protected abstract void loadObject(T object);

    @Override
    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(gson.toJson(getObjectToSave(), targetClass));
        writer.close();
    }

    @Override
    public void load() throws IOException {
        if (file.exists()) {
            try {
                loadObject(gson.fromJson(FileUtils.readFileToString(file), targetClass));
            } catch (MalformedJsonException e) {
                e.printStackTrace();
            }
        }
    }
}
