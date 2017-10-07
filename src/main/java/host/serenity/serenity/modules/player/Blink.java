package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.synapse.Listener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    private List<Vec3> locations = new ArrayList<>();
    private List<Packet> packetsToSend = new ArrayList<>();

    @ModuleValue
    @ValueDescription("Renders a worldline.")
    public BooleanValue line = new BooleanValue("Line", true);

    public Blink() {
        super("Blink", 0x95FFCC, ModuleCategory.PLAYER);
        registerToggleKeybinding(Keyboard.KEY_U);

        listeners.add(new Listener<SendPacket>() {
            @Override
            public void call(SendPacket event) {
                if (!event.isCancelled()) {
                    if (!(event.getPacket() instanceof C00PacketKeepAlive) && !(event.getPacket() instanceof C01PacketChatMessage) && !(event.getPacket() instanceof C16PacketClientStatus && ((C16PacketClientStatus) event.getPacket()).getStatus() == C16PacketClientStatus.EnumState.PERFORM_RESPAWN)) {
                        boolean addToList = true;
                        if (event.getPacket() instanceof C03PacketPlayer) {
                            C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
                            if (packet.isMoving()) {
                                locations.add(new Vec3(packet.getPositionX(), packet.getPositionY(), packet.getPositionZ()));
                            }
                            if (!packet.isMoving() && !packet.getRotating()) {
                                addToList = false;
                            }
                        }
                        if (addToList) {
                            packetsToSend.add(event.getPacket());
                        }
                        event.setCancelled(true);
                    }
                }
            }
        });

        listeners.add(new Listener<RenderWorld>(() -> line.getValue()) {
            @Override
            public void call(RenderWorld event) {
                if (locations.size() > 1) {
                    GL11.glPushMatrix();

                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_LINE_SMOOTH);
                    GL11.glDepthMask(false);
                    GL11.glLineWidth(1.0F);
                    GL11.glColor3f(1, 1, 1);

                    GL11.glBegin(GL11.GL_LINE_STRIP);
                    for (Vec3 loc : locations) {
                        GL11.glVertex3d(loc.xCoord - mc.getRenderManager().viewerPosX, loc.yCoord - mc.getRenderManager().viewerPosY, loc.zCoord - mc.getRenderManager().viewerPosZ);
                    }
                    GL11.glEnd();

                    GL11.glDepthMask(true);
                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);

                    GL11.glPopMatrix();
                }
            }
        });
    }

    @Override
    public void onEnable() {
        packetsToSend.clear();
    }

    @Override
    public void onDisable() {
        for (Packet p : packetsToSend) {
            mc.getNetHandler().getNetworkManager().sendPacket(p);
        }
        packetsToSend.clear();
        locations.clear();
    }
}
