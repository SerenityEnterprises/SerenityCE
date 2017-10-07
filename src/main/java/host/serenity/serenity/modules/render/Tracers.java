package host.serenity.serenity.modules.render;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.render.RenderWorldBobbing;
import host.serenity.synapse.Listener;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class Tracers extends Module {
    @ModuleValue
    private DoubleValue thickness = new DoubleValue("Line Thickness", 0.5);

    @ModuleValue
    private DoubleValue thicknessFriends = new DoubleValue("Friend Line Thickness", 1.2);

    public Tracers() {
        super("Tracers", 0x92FFF8, ModuleCategory.RENDER);
        setHidden(true);

        listeners.add(new Listener<RenderWorldBobbing>() {
            @Override
            public void call(RenderWorldBobbing event) {
                GL11.glPushMatrix();
                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glColor3f(1, 1, 1);

                for (EntityPlayer player : (List<EntityPlayer>) mc.theWorld.playerEntities) {
                    if (player == mc.thePlayer)
                        continue;

                    if (Serenity.getInstance().getFriendManager().isFriend(player.getCommandSenderName())) {
                        GL11.glLineWidth(thicknessFriends.getValue().floatValue());
                        GL11.glColor3f(83 / 255F, 190 / 255F, 246 / 255F);
                    } else {
                        GL11.glLineWidth(thickness.getValue().floatValue());
                        GL11.glColor3f(1, 1, 1);
                    }

                    double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getRenderPartialTicks();
                    double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getRenderPartialTicks();
                    double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getRenderPartialTicks();

                    GL11.glBegin(GL11.GL_LINE_STRIP);

                    GL11.glVertex3d(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY + mc.thePlayer.getEyeHeight(), mc.getRenderManager().viewerPosZ);
                    GL11.glVertex3d(posX, posY, posZ);
                    GL11.glVertex3d(posX, posY + player.getEyeHeight(), posZ);
                    GL11.glEnd();
                }

                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                GL11.glPopMatrix();
            }
        });
    }
}
