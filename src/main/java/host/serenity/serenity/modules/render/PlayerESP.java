package host.serenity.serenity.modules.render;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.render.RenderOverlay;
import host.serenity.serenity.util.render.GLUProjection;
import host.serenity.synapse.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerESP extends Module {
    public PlayerESP() {
        super("Player ESP", 0xFFF478, ModuleCategory.RENDER);
        setHidden(true);

        listeners.add(new Listener<RenderOverlay>() {
            @Override
            public void call(RenderOverlay event) {
                boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);

                GL11.glPushMatrix();

                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glLineWidth(1);

                //noinspection unchecked
                players: for (EntityPlayer player : (List<EntityPlayer>) mc.theWorld.playerEntities) {
                    if (player == mc.thePlayer)
                        continue;

                    List<GLUProjection.Projection> projectionList = new LinkedList<>();
                    AxisAlignedBB playerBB = player.getEntityBoundingBox().expand(0.1, 0.1, 0.1)
                            .offset(-(player.posX - player.lastTickPosX) * (1 - event.getRenderPartialTicks()),
                                    -(player.posY - player.lastTickPosY) * (1 - event.getRenderPartialTicks()),
                                    -(player.posZ - player.lastTickPosZ) * (1 - event.getRenderPartialTicks()))
                            .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                    for (double x = playerBB.minX; x <= playerBB.maxX; x += (playerBB.maxX - playerBB.minX) / 2D) {
                        for (double y = playerBB.minY; y <= playerBB.maxY; y += (playerBB.maxY - playerBB.minY) / 2D) {
                            for (double z = playerBB.minZ; z <= playerBB.maxZ; z += (playerBB.maxZ - playerBB.minZ) / 2D) {
                                GLUProjection.Projection projection = GLUProjection.getInstance().project(x, y, z, GLUProjection.ClampMode.NONE, false);
                                if (projection.isType(GLUProjection.Projection.Type.INVERTED))
                                    continue players;

                                projectionList.add(projection);
                            }
                        }
                    }

                    if (!projectionList.isEmpty()) {
                        List<Double> xCoordinates = projectionList.stream().map(p -> p.getX()).collect(Collectors.toList());
                        double minX = Collections.min(xCoordinates);
                        double maxX = Collections.max(xCoordinates);

                        List<Double> yCoordinates = projectionList.stream().map(p -> p.getY()).collect(Collectors.toList());
                        double minY = Collections.min(yCoordinates);
                        double maxY = Collections.max(yCoordinates);

                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        GL11.glVertex2d(maxX, maxY);
                        GL11.glVertex2d(maxX, minY);
                        GL11.glVertex2d(minX, minY);
                        GL11.glVertex2d(minX, maxY);
                        GL11.glEnd();
                    }
                }

                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                if (blend) {
                    GlStateManager.enableBlend();
                    GL11.glEnable(GL11.GL_BLEND);
                } else {
                    GlStateManager.disableBlend();
                    GL11.glDisable(GL11.GL_BLEND);
                }
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                GL11.glPopMatrix();
            }
        });
    }
}
