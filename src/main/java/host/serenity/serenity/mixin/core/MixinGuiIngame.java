package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.render.RenderOverlay;
import host.serenity.synapse.EventManager;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame {
    @Inject(method = "renderGameOverlay(F)V", at = @At("RETURN"))
    public void onRenderGameOverlay(float renderPartialTicks, CallbackInfo callbackInfo) {
        EventManager.post(new RenderOverlay(renderPartialTicks));
    }
}
