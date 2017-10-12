package host.serenity.serenity.modules.combat;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.FloatValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.util.iface.S27PacketExplosionExtension;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public class AntiKnockback extends Module {

    @ModuleValue
    @ValueDescription("Adds the applied knockback to your current velocity, instead of setting it.")
    public BooleanValue additive = new BooleanValue("additive", true);

    @ModuleValue
    @ValueDescription("Adjusts the relative amount (0 to 1) of the horizontal knockback to be applied.")
    public FloatValue horizontal = new FloatValue("horizontal", 0);

    @ModuleValue
    @ValueDescription("Adjusts the relative amount (0 to 1) of the vertical knockback to be applied.")
    public FloatValue vertical = new FloatValue("vertical", 0);

    public AntiKnockback() {
        super("Anti Knockback", 0xFEFF71, ModuleCategory.COMBAT);

        listeners.add(new Listener<ReceivePacket>() {
            @Override
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                    S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
                    event.setCancelled(true);

                    double motionX = packet.getMotionX() / 8000D * horizontal.getValue();
                    double motionY = packet.getMotionY() / 8000D * vertical.getValue();
                    double motionZ = packet.getMotionZ() / 8000D * horizontal.getValue();

                    if (additive.getValue()) {
                        motionX += mc.thePlayer.motionX;
                        motionY += mc.thePlayer.motionY;
                        motionZ += mc.thePlayer.motionZ;
                    }

                    mc.thePlayer.motionX = motionX;
                    mc.thePlayer.motionY = motionY;
                    mc.thePlayer.motionZ = motionZ;
                }
                if (event.getPacket() instanceof S27PacketExplosion) {
                    S27PacketExplosion packet = (S27PacketExplosion) event.getPacket();

                    float motionX = packet.func_149149_c();
                    float motionY = packet.func_149144_d();
                    float motionZ = packet.func_149147_e();

                    motionX *= horizontal.getValue();
                    motionZ *= vertical.getValue();

                    S27PacketExplosionExtension packetExtension = (S27PacketExplosionExtension) packet;

                    packetExtension.setVelocityX(motionX);
                    packetExtension.setVelocityY(motionY);
                    packetExtension.setVelocityZ(motionZ);
                }
            }
        });
    }
}
