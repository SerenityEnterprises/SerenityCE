package host.serenity.serenity.modules.overlay;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.util.overlay.OverlayArea;
import host.serenity.serenity.util.overlay.OverlayContextManager;

public class Coordinates extends Module {
    public Coordinates() {
        super("Coordinates", 0xF5B7FF, ModuleCategory.OVERLAY);
        setHidden(true);

        OverlayContextManager.INSTANCE.register(OverlayArea.BOTTOM_RIGHT, 500, ctx -> {
            // Do it backwards because we go from the bottom up
            ctx.drawString("Z: " + Math.round(mc.thePlayer.posZ * 10) / 10D, 0xFFFFFF, true);
            ctx.drawString("Y: " + Math.round(mc.thePlayer.posY * 10) / 10D, 0xFFFFFF, true);
            ctx.drawString("X: " + Math.round(mc.thePlayer.posX * 10) / 10D, 0xFFFFFF, true);
        }, this::isEnabled, () -> !mc.gameSettings.showDebugInfo);
    }
}
