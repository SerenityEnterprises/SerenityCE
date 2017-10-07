package host.serenity.serenity.api.plugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private List<Plugin> plugins = new ArrayList<>();

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void tryLoad(Plugin plugin) {
        if (!plugins.contains(plugin))
            plugins.add(plugin);

        try {
            plugin.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tryUnload(Plugin plugin) {
        if (plugins.contains(plugin)) {
            try {
                plugin.unload();
            } catch (Exception e) {
                e.printStackTrace();
            }

            plugins.remove(plugin);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Plugin> T getPlugin(String pluginClassName) {
        for (Plugin plugin : plugins) {
            if (plugin.getClass().getName().equals(pluginClassName)) {
                return (T) plugin;
            }
        }
        return null;
    }
}
