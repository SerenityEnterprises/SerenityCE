package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.NetMovingUpdate;
import host.serenity.serenity.event.player.BlockDigging;
import host.serenity.serenity.event.player.PostBlockDigging;
import host.serenity.synapse.Listener;

/**
 * Created by Jordin on 2/22/2017.
 * Jordin is still best hacker.
 */
public class NoFall extends Module {

    @ModuleValue
    @ValueDescription("Changes the module colour when No Fall is not active.")
    public BooleanValue dynamicColour = new BooleanValue("Dynamic Colour", false);

    private boolean wasOnGround;

    public NoFall() {
        super("No Fall", 0x9AFF93, ModuleCategory.PLAYER);

        listeners.add(new Listener<NetMovingUpdate>() {
            @Override
            public void call(NetMovingUpdate event) {
                event.setOnGround(true);
            }
        });

        listeners.add(new Listener<BlockDigging>() {
            @Override
            public void call(BlockDigging event) {
                wasOnGround = mc.thePlayer.onGround;

                mc.thePlayer.onGround = true;
            }
        });

        listeners.add(new Listener<PostBlockDigging>() {
            @Override
            public void call(PostBlockDigging event) {
                mc.thePlayer.onGround = wasOnGround;
            }
        });
    }

    @Override
    public int getColour() {
        if (!this.isEnabled() || !dynamicColour.getValue())
            return super.getColour();

        return (mc.thePlayer != null && !mc.thePlayer.onGround) ? super.getColour() : 0x656565;
    }
}
