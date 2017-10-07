package host.serenity.serenity.modules.combat;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.synapse.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class Criticals extends Module {
    @ModuleValue
    @ValueDescription("Changes the module colour when Criticals is not active.")
    public BooleanValue dynamicColour = new BooleanValue("Dynamic Colour", true);

    public Criticals() {
        super("Criticals", 0x58CAFF, ModuleCategory.COMBAT);
        registerToggleKeybinding(Keyboard.KEY_N);

        registerMode(new ModuleMode("Packet") {
            private boolean isCritting;

            @ValueDescription("Crits only if the entity's hurtTime is less than this value.")
            private IntValue hurtTime = new IntValue("Hurt Time", 5);

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<SendPacket>() {
                    @Override
                    public void call(SendPacket event) {
                        if (event.getPacket() instanceof C02PacketUseEntity) {
                            C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();

                            if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                                Entity target = packet.getEntityFromWorld(mc.theWorld);
                                if (target != null && target instanceof EntityLivingBase) {
                                    if (!ableToCrit())
                                        return;

                                    if (((EntityLivingBase) target).hurtTime < hurtTime.getValue()) {
                                        if (!isCritting) {
                                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.05, mc.thePlayer.posZ, false));
                                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.012511, mc.thePlayer.posZ, false));
                                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));

                                            isCritting = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        isCritting = false;
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[] { hurtTime };
            }
        });

        setActiveMode("Packet");
    }

    @Override
    public int getColour() {
        if (!this.isEnabled() || !dynamicColour.getValue())
            return super.getColour();

        return (mc.thePlayer != null && ableToCrit()) ? super.getColour() : 0x656565;
    }

    private boolean ableToCrit() {
        return mc.thePlayer.onGround && !BlockHelper.isInLiquid() && !BlockHelper.isOnLiquid();
    }
}
