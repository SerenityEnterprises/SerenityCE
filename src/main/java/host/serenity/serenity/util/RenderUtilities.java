package host.serenity.serenity.util;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class RenderUtilities {
    public static void drawRect(final float x1, final float y1, final float x2,
                                final float y2, final int colour) {
        final float f = (colour >> 24 & 0xFF) / 255F;
        final float f1 = (colour >> 16 & 0xFF) / 255F;
        final float f2 = (colour >> 8 & 0xFF) / 255F;
        final float f3 = (colour & 0xFF) / 255F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glPushMatrix();
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x2, y1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void drawBorderedRect(final float x, final float y,
                                        final float x2, final float y2, final float l1, final int col1,
                                        final int col2) {
        drawRect(x, y, x2, y2, col2);

        final float f = (col1 >> 24 & 0xFF) / 255F;
        final float f1 = (col1 >> 16 & 0xFF) / 255F;
        final float f2 = (col1 >> 8 & 0xFF) / 255F;
        final float f3 = (col1 & 0xFF) / 255F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glPushMatrix();
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glLineWidth(l1);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator var1 = Tessellator.getInstance();
        WorldRenderer var2 = var1.getWorldRenderer();
        var2.startDrawing(3);
        var2.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        var2.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        var2.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        var2.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        var2.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        var1.draw();
        var2.startDrawing(3);
        var2.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        var2.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        var2.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        var2.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        var2.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        var1.draw();
        var2.startDrawing(1);
        var2.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        var2.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        var2.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        var2.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        var2.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        var2.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        var2.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        var2.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        var1.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrender = Tessellator.getInstance().getWorldRenderer();

        worldrender.startDrawingQuads();
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
//		tessellator.draw();
//		worldrender.startDrawingQuads();
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
//		tessellator.draw();
//		worldrender.startDrawingQuads();
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
//		tessellator.draw();
//		worldrender.startDrawingQuads();
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
//		tessellator.draw();
//		worldrender.startDrawingQuads();
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
//		tessellator.draw();
//		worldrender.startDrawingQuads();
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldrender.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        tessellator.draw();
    }

}
