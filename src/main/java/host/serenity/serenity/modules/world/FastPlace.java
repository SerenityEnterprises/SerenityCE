package host.serenity.serenity.modules.world;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.core.RunTick;
import host.serenity.serenity.util.iface.MinecraftExtension;
import host.serenity.synapse.Listener;

public class FastPlace extends Module {
    @ModuleValue
    @ValueDescription("The amount of ticks to wait (Minecraft default is 5)")
    private IntValue delay = new IntValue("delay", 2, 0, 5);

    public FastPlace() {
        super("Fast Place", 0xFF68AA, ModuleCategory.WORLD);

        listeners.add(new Listener<RunTick>() {
            @Override
            public void call(RunTick event) {
                if (((MinecraftExtension) mc).getRightClickDelayTimer() > delay.getValue())
                    ((MinecraftExtension) mc).setRightClickDelayTimer(delay.getValue());
            }
        });
    }
}
