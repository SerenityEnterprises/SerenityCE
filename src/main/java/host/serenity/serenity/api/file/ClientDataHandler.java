package host.serenity.serenity.api.file;

import java.io.IOException;

public interface ClientDataHandler {
    void save() throws IOException;
    void load() throws IOException;
}
