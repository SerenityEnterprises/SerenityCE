package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.render.*;
import host.serenity.serenity.util.iface.EntityRendererExtension;
import host.serenity.serenity.util.mixin.ShaderAccessor;
import host.serenity.synapse.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(EntityRenderer.class)
@Implements(@Interface(iface = EntityRendererExtension.class, prefix = "ext$"))
public abstract class MixinEntityRenderer {
    private static Field isShadowPass = ShaderAccessor.getShadowPassField();

    @Shadow
    private Minecraft mc;

    @Shadow
    private void setupCameraTransform(float partialTicks, int pass) {}

    @Intrinsic(displace = true)
    public void ext$setupCameraTransform(float partialTicks, int pass) {
        setupCameraTransform(partialTicks, pass);
    }

    @Inject(method = "renderHand(FI)V", at = @At("HEAD"), cancellable = true)
    public void onRenderHand(float partialTicks, int xOffset, CallbackInfo callbackInfo) {
        if (isShadowPass != null) {
            try {
                isShadowPass.setBoolean(null, false);
            } catch (IllegalAccessException ignored) {
            }
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        EventManager.post(new RenderWorld(partialTicks));

        GL11.glColor4d(1, 1, 1, 1);
        if (mc.gameSettings.viewBobbing) {
            mc.gameSettings.viewBobbing = false;
            GL11.glPushMatrix();
            setupCameraTransform(partialTicks, 2);
            EventManager.post(new RenderWorldBobbing(partialTicks));
            setupCameraTransform(partialTicks, 2);
            GL11.glPopMatrix();
            mc.gameSettings.viewBobbing = true;
        } else {
            EventManager.post(new RenderWorldBobbing(partialTicks));
        }

        GL11.glColor4d(1, 1, 1, 1);

        GL11.glPopMatrix();
        GL11.glPopAttrib();

        if (EventManager.post(new RenderHand()).isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "updateCameraAndRender(F)V", at = @At("RETURN"))
    public void postRenderEverything(float partialTicks, CallbackInfo callbackInfo) {
        EventManager.post(new RenderEverything(partialTicks));
    }

    @ModifyArg(method = "renderWorldPass(IFJ)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;setupTerrain(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/renderer/culling/ICamera;IZ)V"
    ))
    public boolean isPlayerSpectator(boolean playerSpectator) {
        return playerSpectator || EventManager.post(new Culling()).isCancelled();
    }
}
