package host.serenity.serenity.util.listener;

import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.util.render.GLUProjection;
import host.serenity.synapse.Listener;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLUProjectionUpdater extends Listener<RenderWorld> {
    private static final IntBuffer VIEWPORT_BUFFER = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer MODELVIEW_BUFFER = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION_BUFFER = GLAllocation.createDirectFloatBuffer(16);

    @Override
    public void call(RenderWorld event) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        GLUProjection.getInstance().updateMatrices(VIEWPORT_BUFFER, MODELVIEW_BUFFER, PROJECTION_BUFFER,
                scaledResolution.getScaledWidth() / (double) mc.displayWidth,
                scaledResolution.getScaledHeight() / (double) mc.displayHeight);
    }
}
