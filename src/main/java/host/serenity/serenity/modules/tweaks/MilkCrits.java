package host.serenity.serenity.modules.tweaks;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.serenity.util.iface.MinecraftExtension;
import host.serenity.synapse.Listener;
import net.minecraft.util.Timer;

public class MilkCrits extends Module {
    public MilkCrits() {
        super("Milk Crits", 0xFFE2FB, ModuleCategory.TWEAKS);

        listeners.add(new Listener<PlayerUpdate>() {
            @Override
            public void call(PlayerUpdate event) {
                Timer timer = ((MinecraftExtension) mc).getTimer();
                timer.timerSpeed = 1F;

                if (Math.abs(mc.thePlayer.motionY) > 0.1) {
                    if (mc.thePlayer.motionY > 0) {
                        timer.timerSpeed = 1.5F;
                    } else if (mc.thePlayer.motionY < 0) {
                        timer.timerSpeed = 0.6F;
                    }
                }
            }
        });
    }
}
