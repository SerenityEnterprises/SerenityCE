package host.serenity.serenity.modules.render;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.synapse.Listener;
import org.lwjgl.opengl.GL11;

public class ChunkBorders extends Module {
    @ModuleValue
    public IntValue heightValue = new IntValue("height", 64);

    public ChunkBorders() {
        super("Chunk Borders", 0xFCD2FF, ModuleCategory.RENDER);
        setHidden(true);

        listeners.add(new Listener<RenderWorld>() {
            @Override
            public void call(RenderWorld event) {
                int chunkX, chunkZ;
                chunkX = (int) Math.floor(mc.thePlayer.posX / 16D) * 16;
                chunkZ = (int) Math.floor(mc.thePlayer.posZ / 16D) * 16;

                GL11.glPushMatrix();
                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                GL11.glLineWidth(1F);
                GL11.glColor3f(1, 1, 1);


                int height = heightValue.getValue();

                if (height < 0) {
                    height = (int) Math.floor(mc.getRenderManager().viewerPosY);
                }

                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        GL11.glBegin(GL11.GL_LINE_STRIP);
                        GL11.glVertex3d(chunkX + x * 16, height, chunkZ + z * 16);
                        GL11.glVertex3d(chunkX + 16 + x * 16, height, chunkZ + z * 16);
                        GL11.glVertex3d(chunkX + 16 + x * 16, height, chunkZ + 16 + z * 16);
                        GL11.glVertex3d(chunkX + x * 16, height, chunkZ + 16 + z * 16);
                        GL11.glVertex3d(chunkX + x * 16, height, chunkZ + z * 16);
                        GL11.glEnd();
                    }
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
