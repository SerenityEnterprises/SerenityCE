package host.serenity.serenity.modules.world;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.BlockDigging;
import host.serenity.synapse.Listener;

public class Speedmine extends Module {
    @ModuleValue
    @ValueDescription("Block mining delay.")
    private IntValue delay = new IntValue("Delay", 0);

    public Speedmine() {
        super("Speedmine", 0x41FFAA, ModuleCategory.WORLD);

        listeners.add(new Listener<BlockDigging>() {
            @Override
            public void call(BlockDigging event) {
                if (event.getHitDelay() > delay.getValue()) {
                    event.setHitDelay(delay.getValue());
                }
            }
        });
    }
}
