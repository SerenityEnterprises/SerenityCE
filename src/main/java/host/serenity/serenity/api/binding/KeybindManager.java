package host.serenity.serenity.api.binding;

import host.serenity.serenity.event.internal.KeyEvent;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;

import java.util.ArrayList;
import java.util.List;

public class KeybindManager {
    private List<Keybinding> bindings = new ArrayList<>();

    public KeybindManager() {
        EventManager.register(new Listener<KeyEvent>() {
            @Override
            public void call(KeyEvent event) {
                for (Keybinding binding : bindings) {
                    if (event.getKey() == binding.getKey()) {
                        binding.updateState(event.getPressed());
                    }
                }
            }
        });
    }

    public void register(Keybinding keybinding) {
        bindings.add(keybinding);
    }

    public List<Keybinding> getBindings() {
        return bindings;
    }
}
