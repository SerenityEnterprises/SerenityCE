package host.serenity.serenity.api.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final File CLIENT_DIRECTORY = new File(System.getProperty("user.home") + File.separator + "Serenity");

    public static File getClientDirectory() {
        return CLIENT_DIRECTORY;
    }

    public static File createClientFile(String name) {
        return new File(CLIENT_DIRECTORY, name);
    }

    private List<ClientDataHandler> dataHandlerList = new ArrayList<>();

    public void createClientDirectory() {
        CLIENT_DIRECTORY.mkdirs();
    }

    public void loadAll() {
        dataHandlerList.forEach(dataHandler -> {
            try {
                dataHandler.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void saveAll() {
        dataHandlerList.forEach(dataHandler -> {
            try {
                dataHandler.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void registerDataHandler(ClientDataHandler dataHandler) {
        dataHandlerList.add(dataHandler);
    }

    public List<ClientDataHandler> getDataHandlerList() {
        return dataHandlerList;
    }
}
