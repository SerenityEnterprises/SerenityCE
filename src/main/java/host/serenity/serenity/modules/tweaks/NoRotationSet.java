package host.serenity.serenity.modules.tweaks;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.network.PostReceivePacket;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class NoRotationSet extends Module {
    private float yaw, pitch, lastYaw, lastPitch;
    private boolean wasSet;

    public NoRotationSet() {
        super("No Rotation Set", 0x6EFF6A, ModuleCategory.TWEAKS);
        setHidden(true);

        listeners.add(new Listener<ReceivePacket>() {
            @Override
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                    if (mc.thePlayer.rotationYaw != -180 || mc.thePlayer.rotationPitch != 0) {
                        lastYaw = mc.thePlayer.prevRotationYaw;
                        lastPitch = mc.thePlayer.prevRotationPitch;
                        yaw = mc.thePlayer.rotationYaw;
                        pitch = mc.thePlayer.rotationPitch;

                        wasSet = true;

                        // ((S08PacketPlayerPosLookExtension) event.getPacket()).setYaw(mc.thePlayer.rotationYaw);
                        // ((S08PacketPlayerPosLookExtension) event.getPacket()).setPitch(mc.thePlayer.rotationPitch);
                    }
                }
            }
        });

        listeners.add(new Listener<PostReceivePacket>() {
            @Override
            public void call(PostReceivePacket event) {
                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                    if (wasSet) {
                        wasSet = false;

                        mc.thePlayer.prevRotationYaw = lastYaw;
                        mc.thePlayer.prevRotationPitch = lastPitch;

                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                    }
                }
            }
        });

        setState(true);
    }
}
