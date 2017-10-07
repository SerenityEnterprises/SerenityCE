package host.serenity.serenity.modules.movement;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.PostSendPacket;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {
    @ModuleValue
    @ValueDescription("Allows the player to sprint in all directions.")
    public BooleanValue omnidirectional = new BooleanValue("Omnidirectional", false);

    public Sprint() {
        super("Sprint", 0x76E191, ModuleCategory.MOVEMENT);
        registerToggleKeybinding(Keyboard.KEY_V);

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                mc.thePlayer.setSprinting(shouldSprint());
            }
        });

        listeners.add(new Listener<PostSendPacket>() {
            @Override
            public void call(PostSendPacket event) {
                if (event.getPacket() instanceof C02PacketUseEntity) {
                    if (((C02PacketUseEntity) event.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK) {
                        if (shouldSprint()) {
                            mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING)); // ok cool
                            mc.thePlayer.setSprinting(true);
                        }
                    }
                }
            }
        });
    }

    public boolean shouldSprint() {
        boolean moving = Math.abs(mc.thePlayer.movementInput.moveForward) > 0.1 || Math.abs(mc.thePlayer.movementInput.moveStrafe) > 0.1;

        boolean using = mc.thePlayer.isUsingItem() && !Serenity.getInstance().getModuleManager().getModule(NoSlowdown.class).isEnabled();
        return (mc.thePlayer.moveForward > 0 || (omnidirectional.getValue() && moving)) && mc.thePlayer.getFoodStats().getFoodLevel() > 6 && !mc.thePlayer.isInWater() && !mc.thePlayer.isSneaking() && !using;
    }
}
