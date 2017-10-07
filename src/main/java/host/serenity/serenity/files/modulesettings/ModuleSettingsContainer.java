package host.serenity.serenity.files.modulesettings;

import java.util.HashMap;
import java.util.Map;

public class ModuleSettingsContainer {
    public boolean enabled;

    public Map<String, String> values = new HashMap<>();

    public Map<String, Map<String, String>> modes = new HashMap<>();
    public String mode = null;
}
