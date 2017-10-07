package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Jordin on 2/13/2017.
 * Jordin is still best hacker.
 */
public class InventoryWalk extends Module {
    @ModuleValue
    @ValueDescription("Allows you to move around in inventories.")
    private BooleanValue movement = new BooleanValue("Movement", true);

    /*@ModuleValue
    private BooleanValue rotation = new BooleanValue("Rotation", true);
    */

    public InventoryWalk() {
        super("Inventory Walk", 0x2850e0, ModuleCategory.PLAYER);

        setHidden(true);

        List<KeyBinding> keys = Arrays.asList(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump);

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                if (movement.getValue()) {
                    keys.forEach(key -> KeyBinding.setKeyBindState(key.getKeyCode(), !Keyboard.areRepeatEventsEnabled() && Keyboard.isKeyDown(key.getKeyCode())));
                }

                // FIXME: this doesn't work due to how you spoof rotations
                // It rotates you a bit but then it rotates you back.
                /*if (rotation.getValue() && mc.currentScreen != null && !Keyboard.areRepeatEventsEnabled() && Keyboard.isKeyDown(Keyboard.KEY_R)) {
                    MouseHelper mouseHelper = mc.mouseHelper;
                    mouseHelper.mouseXYChange();
                    float sensitivity = (mc.gameSettings.mouseSensitivity * 0.6F) + 0.2F;
                    float scale = sensitivity * sensitivity * sensitivity * 8.0F;
                    float motionX = mouseHelper.deltaX * scale;
                    float motionY = mouseHelper.deltaY * scale;
                    mc.thePlayer.setAngles(motionX, motionY * (mc.gameSettings.invertMouse ? -1 : 1));
                }*/
            }
        });
    }


}
