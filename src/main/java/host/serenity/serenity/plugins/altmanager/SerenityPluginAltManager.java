package host.serenity.serenity.plugins.altmanager;

import host.serenity.serenity.api.plugin.Plugin;
import host.serenity.serenity.event.render.RenderEverything;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.gui.GuiMainMenu;

import static host.serenity.serenity.Serenity.BUILD_NUMBER;

public class SerenityPluginAltManager implements Plugin {
    private static AccountManager accountManager;

    private AltManagerMainMenuHook mainMenuHook;
    private Listener<?> helpTextInfo;

    @Override
    public void load() {
        accountManager = new AccountManager();

        mainMenuHook = new AltManagerMainMenuHook();

        helpTextInfo = new Listener<RenderEverything>() {
            @Override
            public void call(RenderEverything event) {
                if (mc.currentScreen instanceof GuiMainMenu) {
                    mc.fontRendererObj.drawStringWithShadow("Serenity (Community Edition) b" + BUILD_NUMBER, 2, 2, 0x07A4FE);
                    mc.fontRendererObj.drawStringWithShadow("Press both shifts to show the account manager.", 2, 12, 0xFFFFFF);
                }
            }
        };

        EventManager.register(mainMenuHook);
        EventManager.register(helpTextInfo);
    }

    @Override
    public void unload() {
        EventManager.unregister(helpTextInfo);
        EventManager.unregister(mainMenuHook);
        mainMenuHook = null;

        accountManager = null;
    }

    public static AccountManager getAccountManager() {
        return accountManager;
    }
}
