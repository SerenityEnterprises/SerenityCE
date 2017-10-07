package host.serenity.serenity.modules.combat;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.Listener;
import net.minecraft.client.settings.KeyBinding;

/**
 * Created by jordin on 7/26/17.
 */
public class AutoClicker extends Module {
    @ModuleValue
    @ValueDescription("Adjusts the minimum times to click per second.")
    private final DoubleValue minCPS = new DoubleValue("Min CPS", 8, 0, 20);

    @ModuleValue
    @ValueDescription("Adjusts the maximum times to click per second.")
    private final DoubleValue maxCPS = new DoubleValue("Max CPS", 12, 0, 20);

    private TimeHelper timer = new TimeHelper();

    private int delay;

    public AutoClicker() {
        super("Auto Clicker", 0xb22c2c, ModuleCategory.COMBAT);
        listeners.add(new Listener<PlayerUpdate>() {
            @Override
            public void call(PlayerUpdate event) {
                if (maxCPS.getValue() < minCPS.getValue()) {
                    maxCPS.setValue(minCPS.getValue());
                }
                if (timer.hasReached(delay)) {
                    timer.reset();
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

                    double min = minCPS.getValue();
                    double delta = maxCPS.getValue() - min;

                    double nextDelay = 1 / (min + Math.random() * delta);
                    delay = (int) Math.floor(nextDelay * 1000);
                }
            }
        });
    }

}
