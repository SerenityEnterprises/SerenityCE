package host.serenity.serenity.modules.render;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;

public class Brightness extends Module {
    public Brightness() {
        super("Brightness", 0xFFE771, ModuleCategory.RENDER);
    }

    @Override
    protected void onEnable() {
        mc.gameSettings.gammaSetting += 1000;
    }

    @Override
    protected void onDisable() {
        mc.gameSettings.gammaSetting -= 1000;
    }
}
