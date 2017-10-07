package host.serenity.serenity.files.bindings;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.binding.Keybinding;
import host.serenity.serenity.api.binding.impl.CommandKeybinding;
import host.serenity.serenity.api.binding.impl.ModuleKeybinding;
import host.serenity.serenity.api.file.FileManager;
import host.serenity.serenity.api.file.handler.AbstractJsonDataHandler;
import host.serenity.serenity.api.module.Module;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BindingDataHandler extends AbstractJsonDataHandler<BindingDataContainer> {
    public BindingDataHandler() {
        super(BindingDataContainer.class, FileManager.createClientFile("binds.json"));
    }

    @Override
    protected BindingDataContainer getObjectToSave() {
        BindingDataContainer dataContainer = new BindingDataContainer();

        for (Keybinding binding : Serenity.getInstance().getKeybindManager().getBindings()) {
            String key = Keyboard.getKeyName(binding.getKey());

            dataContainer.keybindings.putIfAbsent(key, new ArrayList<>());
            Map<String, String> keybindAttributes = new HashMap<>();

            if (binding instanceof ModuleKeybinding) {
                ModuleKeybinding moduleKeybinding = (ModuleKeybinding) binding;
                keybindAttributes.put("identifier", "module");
                keybindAttributes.put("module", moduleKeybinding.getModule().getName().toLowerCase().replace(" ", ""));
                keybindAttributes.put("type", moduleKeybinding.getType().name());
            }
            if (binding instanceof CommandKeybinding) {
                CommandKeybinding commandKeybinding = (CommandKeybinding) binding;
                keybindAttributes.put("identifier", "command");
                keybindAttributes.put("command", commandKeybinding.getCommand());
            }

            dataContainer.keybindings.get(key).add(keybindAttributes);
        }

        return dataContainer;
    }

    @Override
    protected void loadObject(BindingDataContainer object) {
        Serenity.getInstance().getKeybindManager().getBindings().clear();

        for (Map.Entry<String, List<Map<String, String>>> keybindListEntry : object.keybindings.entrySet()) {
            try {
                int key = Keyboard.getKeyIndex(keybindListEntry.getKey());

                for (Map<String, String> keybindingAttributes : keybindListEntry.getValue()) {
                    try {
                        if (keybindingAttributes.get("identifier").equals("module")) {
                            String moduleName = keybindingAttributes.get("module");
                            Module module = null;

                            for (Module possibleModule : Serenity.getInstance().getModuleManager().getModules()) {
                                if (possibleModule.getName().toLowerCase().replace(" ", "").equals(moduleName)) {
                                    module = possibleModule;
                                    break;
                                }
                            }

                            ModuleKeybinding.Type type = ModuleKeybinding.Type.valueOf(keybindingAttributes.get("type"));

                            if (module != null) {
                                Serenity.getInstance().getKeybindManager().register(new ModuleKeybinding(module, key, type));
                            }
                        }
                        if (keybindingAttributes.get("identifier").equals("command")) {
                            Serenity.getInstance().getKeybindManager().register(new CommandKeybinding(key, keybindingAttributes.get("command")));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
