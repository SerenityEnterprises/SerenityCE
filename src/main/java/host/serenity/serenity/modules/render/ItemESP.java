package host.serenity.serenity.modules.render;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.synapse.Listener;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ItemESP extends Module {
    public ItemESP() {
        super("Item ESP", 0xFFBEEF, ModuleCategory.RENDER);
        setHidden(true);

        listeners.add(new Listener<RenderWorld>() {
            @Override
            public void call(RenderWorld event) {
                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDepthMask(false);
                GL11.glLineWidth(1.0F);

                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                for (Object o : mc.theWorld.loadedEntityList) {
                    if (o instanceof EntityItem) {
                        EntityItem entityItem = (EntityItem) o;

                        double posX = entityItem.lastTickPosX + (entityItem.posX - entityItem.lastTickPosX) * event.getRenderPartialTicks();
                        double posY = entityItem.lastTickPosY + (entityItem.posY - entityItem.lastTickPosY) * event.getRenderPartialTicks();
                        double posZ = entityItem.lastTickPosZ + (entityItem.posZ - entityItem.lastTickPosZ) * event.getRenderPartialTicks();

                        GL11.glColor4d(1, 1, 1, 0.2);

                        if (entityItem.getEntityItem() != null) {
                            ItemStack stack = entityItem.getEntityItem();
                            Color color = new Color(stack.getItem().getColorFromItemStack(stack, 1));

                            GL11.glColor4d(color.getRed(), color.getGreen(), color.getBlue(), 0.2);
                        }

                        AxisAlignedBB boundingBox = new AxisAlignedBB(posX - 0.25, posY, posZ - 0.25,
                                posX + 0.25, posY + 0.5, posZ + 0.25);

                        RenderUtilities.drawBoundingBox(boundingBox);
                        RenderUtilities.drawOutlinedBoundingBox(boundingBox);
                    }
                }

                GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);

                GL11.glDepthMask(true);
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
