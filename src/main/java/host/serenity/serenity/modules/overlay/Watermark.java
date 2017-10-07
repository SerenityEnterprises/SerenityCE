package host.serenity.serenity.modules.overlay;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.help.ModuleDescription;
import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.util.overlay.OverlayArea;
import host.serenity.serenity.util.overlay.OverlayContextManager;

@ModuleDescription("Displays the client's name and build number on the HUD.")
public class Watermark extends Module {
    @ModuleValue
    @ValueDescription("Displays the build number next to the watermark.")
    private BooleanValue showBuild = new BooleanValue("Show Build", true);

    public Watermark() {
        super("Watermark", 0x61FF5D, ModuleCategory.OVERLAY);
        setHidden(true);

        OverlayContextManager.INSTANCE.register(OverlayArea.TOP_LEFT, 1000, ctx -> {
            ctx.drawString("Serenity (Community Edition)" + (showBuild.getValue() ? " b" + Serenity.BUILD_NUMBER : ""), 0x07A4FE, true);
            ctx.drawString("Copyright (c) 2017 Serenity Enterprises", 0x4AFFFFFF, true);
        }, this::isEnabled, () -> !mc.gameSettings.showDebugInfo);

        setState(true);
    }
}
