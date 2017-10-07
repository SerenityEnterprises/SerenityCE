package host.serenity.serenity.api.binding.impl;

import host.serenity.serenity.api.binding.Keybinding;
import host.serenity.serenity.api.module.Module;

public class ModuleKeybinding extends Keybinding {
    private Type type;
    private final Module module;

    public ModuleKeybinding(Module module, int key, Type type) {
        super(key);
        this.type = type;
        this.module = module;
    }

    @Override
    public void updateState(boolean state) {
        switch (this.getType()) {
            case HOLD:
                module.setState(state);
                break;
            case TOGGLE:
                if (state) {
                    module.setState(!module.isEnabled());
                }
                break;
        }
    }

    public Type getType() {
        return type;
    }

    public Module getModule() {
        return module;
    }

    public enum Type {
        HOLD, TOGGLE
    }
}
