package host.serenity.serenity.plugins.gui;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.gui.GuiManager;
import host.serenity.serenity.api.gui.component.Panel;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.plugin.Plugin;
import host.serenity.serenity.event.internal.KeyEvent;
import host.serenity.serenity.plugins.gui.component.ModuleButton;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SerenityPluginGui implements Plugin {
    private static GuiManager guiManager;
    private boolean wasSetUp;

    private Listener<KeyEvent> keyEventListener = new Listener<KeyEvent>() {
        @Override
        public void call(KeyEvent event) {
            int key = Keyboard.KEY_RSHIFT;
            if (event.getKey() == key) {
                if (mc.currentScreen == null) {
                    for (KeyBinding keyBinding : mc.gameSettings.keyBindings)
                        if (keyBinding.getKeyCode() == key)
                            return;
                    if (!wasSetUp) {
                        setup(guiManager);
                        wasSetUp = true;
                    }
                    mc.displayGuiScreen(new GuiSerenity(guiManager));
                }
            }
        }
    };

    @Override
    public void load() {
        guiManager = new GuiManager();
        wasSetUp = false;

        EventManager.register(keyEventListener);
    }

    @Override
    public void unload() {
        EventManager.unregister(keyEventListener);
        guiManager = null;
    }

    private static void setup(GuiManager guiManager) {
        Random random = new Random();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

        Map<ModuleCategory, Panel> categoryPanelMap = new HashMap<>();
        for (Module module : Serenity.getInstance().getModuleManager().getModules()) {
            if (module.getCategory().getDisplayInGui()) {
                if (!categoryPanelMap.containsKey(module.getCategory()))
                    categoryPanelMap.put(module.getCategory(), new Panel(module.getCategory().getHumanizedName(), random.nextInt(scaledResolution.getScaledWidth() - 25) + 25, random.nextInt(scaledResolution.getScaledHeight() - 25) + 25, 75, 150));

                Panel panel = categoryPanelMap.get(module.getCategory());
                panel.getComponents().add(new ModuleButton(module));
            }
        }

        int x = 2;
        int y = 15;
        int maxHeight = 0;

        for (Panel panel : categoryPanelMap.values()) {
            panel.resizePanelToChildren();

            maxHeight = Math.max(maxHeight, panel.getHeight());

            panel.setX(x);
            panel.setY(y);

            x += panel.getWidth() + 2;
            if (x > scaledResolution.getScaledWidth() - 25) {
                y += maxHeight;
                maxHeight = 0;
                x = 25;
            }
        }

        categoryPanelMap.values().forEach(guiManager.getPanels()::add);
    }

    public static GuiManager getGuiManager() {
        return guiManager;
    }
}
