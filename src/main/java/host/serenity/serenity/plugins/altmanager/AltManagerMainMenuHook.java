package host.serenity.serenity.plugins.altmanager;

import host.serenity.serenity.event.core.RunTick;
import host.serenity.serenity.plugins.altmanager.gui.GuiAltManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.gui.GuiMainMenu;
import org.lwjgl.input.Keyboard;

public class AltManagerMainMenuHook extends Listener<RunTick> {
    @Override
    public void call(RunTick event) {
        if (mc.currentScreen instanceof GuiMainMenu) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                mc.displayGuiScreen(new GuiAltManager());
            }
        }
    }
}
