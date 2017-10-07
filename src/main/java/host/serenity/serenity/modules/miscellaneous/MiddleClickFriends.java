package host.serenity.serenity.modules.miscellaneous;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.core.RunTick;
import host.serenity.synapse.Listener;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

public class MiddleClickFriends extends Module {
    private boolean isDown;

    public MiddleClickFriends() {
        super("Middle Click Friends", 0x3FB3FF, ModuleCategory.MISCELLANEOUS);
        setHidden(true);

        listeners.add(new Listener<RunTick>() {
            @Override
            public void call(RunTick event) {
                if (!mc.inGameHasFocus)
                    return;

                if (Mouse.isButtonDown(2) && !isDown) {
                    isDown = true;

                    if (mc.objectMouseOver.entityHit != null) {
                        if (mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                            EntityPlayer player = (EntityPlayer) mc.objectMouseOver.entityHit;
                            if (Serenity.getInstance().getFriendManager().isFriend(player.getCommandSenderName())) {
                                Serenity.getInstance().getFriendManager().delFriend(player.getCommandSenderName());
                            } else {
                                Serenity.getInstance().getFriendManager().addFriend(player.getCommandSenderName());
                            }
                        }
                    }
                } else if (!Mouse.isButtonDown(2)) {
                    isDown = false;
                }
            }
        });

        setState(true);
    }
}
