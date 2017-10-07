package host.serenity.serenity.modules.miscellaneous;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.client.gui.GuiGameOver;

public class Ghost extends Module {
    private boolean hasGhosted;

    public Ghost() {
        super("Ghost", 0xC0FFB1, ModuleCategory.MISCELLANEOUS);

        listeners.add(new Listener<PlayerUpdate>() {
            @Override
            public void call(PlayerUpdate event) {
                if (mc.currentScreen instanceof GuiGameOver) {
                    mc.currentScreen = null;
                    mc.setIngameFocus();

                    hasGhosted = true;
                    mc.thePlayer.setPlayerSPHealth(1);
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        if (hasGhosted) {
            mc.thePlayer.setPlayerSPHealth(0);
            hasGhosted = false;
        }
    }
}
