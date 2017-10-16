package host.serenity.serenity.files.friends;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.file.ClientDataHandler;
import host.serenity.serenity.api.file.FileManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FriendsDataHandler implements ClientDataHandler {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File file = FileManager.createClientFile("friends.json");


    @Override
    public void save() throws IOException {
        String json = gson.toJson(Serenity.getInstance().getFriendManager().getFriends());
        FileUtils.write(file, json);
    }

    @Override
    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }

        String json = FileUtils.readFileToString(file);
        Map<String, String> friends = gson.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());

        Serenity.getInstance().getFriendManager().getFriends().clear();
        Serenity.getInstance().getFriendManager().getFriends().putAll(friends);
    }
}
