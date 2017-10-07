package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.FloatValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.serenity.util.iface.MinecraftExtension;
import host.serenity.synapse.Listener;

/**
 * Created by jordin on 7/26/17.
 */
public class Timer extends Module {
    @ModuleValue
    @ValueDescription("The speed to run the game (vanilla is 1).")
    private FloatValue speed = new FloatValue("Speed", 1.25F);

    public Timer() {
        super("Timer", 0x49af1a, ModuleCategory.MOVEMENT);
        listeners.add(new Listener<PlayerUpdate>() {
            @Override
            public void call(PlayerUpdate event) {
                if (speed.getValue() == 0) {
                    speed.setValue(1F);
                }
                ((MinecraftExtension) mc).getTimer().timerSpeed = speed.getValue();
            }
        });
    }

    @Override
    public void onDisable() {
        ((MinecraftExtension) mc).getTimer().timerSpeed = 1;
    }
}