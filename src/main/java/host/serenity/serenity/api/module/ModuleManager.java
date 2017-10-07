package host.serenity.serenity.api.module;

import host.serenity.serenity.api.help.GenericDescription;

import java.util.ArrayList;
import java.util.List;

@GenericDescription("A module is a single cheat that one can toggle on and off. " +
        "A module can contain values which one can tweak in order to edit how the module functions.")
public class ModuleManager {
    private List<Module> modules = new ArrayList<Module>() {
        @Override
        public boolean add(Module module) {
            boolean result = super.add(module);
            sortModules();
            return result;
        }

        @Override
        public boolean remove(Object o) {
            boolean result = super.remove(o);
            sortModules();
            return result;
        }
    };

    public List<Module> getModules() {
        return modules;
    }

    public <T extends Module> T getModule(Class<T> moduleClass) {
        for (Module module : modules) {
            if (module.getClass() == moduleClass) {
                return moduleClass.cast(module);
            }
        }

        return null;
    }

    public void sortModules() {
        modules.sort((module1, module2) -> String.CASE_INSENSITIVE_ORDER.compare(module1.getName(), module2.getName()));
    }

}
