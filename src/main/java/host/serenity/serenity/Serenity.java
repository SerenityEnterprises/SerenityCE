package host.serenity.serenity;

import host.serenity.serenity.api.binding.KeybindManager;
import host.serenity.serenity.api.command.CommandManager;
import host.serenity.serenity.api.file.FileManager;
import host.serenity.serenity.api.friend.FriendManager;
import host.serenity.serenity.api.module.ModuleManager;
import host.serenity.serenity.api.plugin.PluginManager;
import host.serenity.serenity.event.internal.GameShutdown;
import host.serenity.serenity.event.internal.PostGameShutdown;
import host.serenity.serenity.util.SerenityBootstrapper;
import host.serenity.serenity.util.listener.GLUProjectionUpdater;
import host.serenity.serenity.util.listener.dispatcher.NetworkMovingDispatcher;
import host.serenity.serenity.util.overlay.OverlayContextManager;
import host.serenity.serenity.util.prediction.PredictionEngine;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Serenity {
    public static final int BUILD_NUMBER = 1;

    public static Serenity instance;
    public static Serenity getInstance() {
        return instance;
    }

    private PredictionEngine predictionEngine;
    private CommandManager commandManager;
    private ModuleManager moduleManager;
    private KeybindManager keybindManager;
    private PluginManager pluginManager;
    private FriendManager friendManager;
    private FileManager fileManager;

    public Serenity() {
        instance = this;

        predictionEngine = new PredictionEngine();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        keybindManager = new KeybindManager();
        pluginManager = new PluginManager();
        friendManager = new FriendManager();
        fileManager = new FileManager();

        new SerenityBootstrapper().run();
        OverlayContextManager.INSTANCE._registerEvents();

        EventManager.register(new Listener<GameShutdown>() {
            @Override
            public void call(GameShutdown event) {
                fileManager.saveAll();
                pluginManager.getPlugins().forEach(plugin -> {
                    try {
                        plugin.unload();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                moduleManager.getModules().forEach(module -> {
                    if (module.isEnabled())
                        module.setState(false);
                });

                PostGameShutdown postShutdownEvent = EventManager.post(new PostGameShutdown());
                EventManager.getListeners().clear();
                postShutdownEvent.getTasks().forEach(Runnable::run);
            }
        });

        EventManager.register(new NetworkMovingDispatcher());
        EventManager.register(new GLUProjectionUpdater());
    }

    public void addChatMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "[S] " + EnumChatFormatting.RESET + message));
    }

    public PredictionEngine getPredictionEngine() {
        return predictionEngine;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }
}
