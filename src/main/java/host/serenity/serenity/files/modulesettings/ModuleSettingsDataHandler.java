package host.serenity.serenity.files.modulesettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.file.ClientDataHandler;
import host.serenity.serenity.api.file.FileManager;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.Value;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ModuleSettingsDataHandler implements ClientDataHandler {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private File settingsDirectory = FileManager.createClientFile("module_settings"); {
        if (!settingsDirectory.exists()) {
            settingsDirectory.mkdir();
        }
    }

    @Override
    public void save() throws IOException {
        for (Module module : Serenity.getInstance().getModuleManager().getModules()) {
            ModuleSettingsContainer settingsContainer = new ModuleSettingsContainer();
            settingsContainer.enabled = module.isEnabled();

            for (Value<?> value : module.getValues()) {
                settingsContainer.values.put(value.getName(), value.getValue().toString());
            }

            if (module.getActiveMode() != null) {
                settingsContainer.mode = module.getActiveMode().getName().toLowerCase();
                for (ModuleMode mode : module.getModuleModes()) {
                    Map<String, String> modeValues = new HashMap<>();
                    for (Value<?> value : mode.getValues()) {
                        modeValues.put(value.getName(), value.getValue().toString());
                    }

                    settingsContainer.modes.put(mode.getName().toLowerCase().replace(" ", ""), modeValues);
                }
            }

            File file = new File(settingsDirectory, module.getName().toLowerCase().replace(" ", "") + ".json");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(gson.toJson(settingsContainer));
            writer.close();
        }
    }

    @Override
    public void load() throws IOException {
        for (Module module : Serenity.getInstance().getModuleManager().getModules()) {
            try {
                File file = new File(settingsDirectory, module.getName().toLowerCase().replace(" ", "") + ".json");
                if (file.exists()) {
                    String json = FileUtils.readFileToString(file);
                    ModuleSettingsContainer container = gson.fromJson(json, ModuleSettingsContainer.class);

                    module.setState(container.enabled);

                    Map<String, Value<?>> valueNameMap = new HashMap<>();
                    for (Value<?> value : module.getValues()) {
                        valueNameMap.put(value.getName(), value);
                    }

                    for (Map.Entry<String, String> entry : container.values.entrySet()) {
                        valueNameMap.get(entry.getKey()).setValueFromString(entry.getValue());
                    }

                    if (container.mode != null) {
                        // module.setActiveMode(container.mode);
                        for (ModuleMode mode : module.getModuleModes()) {
                            if (mode.getName().equalsIgnoreCase(container.mode)) {
                                module.setActiveMode(mode);
                            }
                        }
                    }

                    for (Map.Entry<String, Map<String, String>> entry : container.modes.entrySet()) {
                        String modeName = entry.getKey();
                        ModuleMode mode = null;
                        for (ModuleMode moduleMode : module.getModuleModes()) {
                            if (moduleMode.getName().toLowerCase().replace(" ", "").equals(modeName)) {
                                mode = moduleMode;
                            }
                        }

                        if (mode != null) {
                            Map<String, String> valueMap = entry.getValue();
                            for (Map.Entry<String, String> valueEntry : valueMap.entrySet()) {
                                for (Value<?> value : mode.getValues()) {
                                    if (value.getName().equals(valueEntry.getKey())) {
                                        value.setValueFromString(valueEntry.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
