package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.render.RenderGuiScreen;
import host.serenity.synapse.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by jordin on 6/26/17.
 */
@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {
    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    public void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();

        GuiScreen thisScreen = (GuiScreen) (Object) this;

        RenderGuiScreen renderGuiScreen = new RenderGuiScreen(thisScreen);
        EventManager.post(renderGuiScreen);

        if (renderGuiScreen.isCancelled()) {
            if (mc.currentScreen != null && mc.currentScreen != thisScreen) {
                mc.currentScreen.drawScreen(mouseX, mouseY, partialTicks);
            }
            ci.cancel();
        }

    }
}
