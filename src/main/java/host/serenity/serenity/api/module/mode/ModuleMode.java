package host.serenity.serenity.api.module.mode;

import host.serenity.serenity.api.value.Value;
import host.serenity.synapse.Listener;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class ModuleMode {
    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;

    private final List<Listener<?>> listeners = new ArrayList<>();

    public ModuleMode(String name) {
        this.name = name;

        addListeners(listeners);
    }

    public abstract void addListeners(List<Listener<?>> listeners);
    public abstract Value<?>[] getValues();

    public String getName() {
        return name;
    }

    public List<Listener<?>> getListeners() {
        return listeners;
    }

    public void onEnable() {}
    public void onDisable() {}
}
