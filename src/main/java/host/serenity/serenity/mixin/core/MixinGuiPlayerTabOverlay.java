package host.serenity.serenity.mixin.core;

import host.serenity.serenity.Serenity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay {
    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    public int onDrawStringWithShadow(FontRenderer fontRenderer, String string, float x, float y, int colour) {
        try {
            string = Serenity.getInstance().getFriendManager().applyProtection(string);
        } catch (Exception ignored) {}
        return fontRenderer.drawStringWithShadow(string, x, y, colour);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I"))
    public int onGetStringWidth(FontRenderer fontRenderer, String string) {
        try {
            string = Serenity.getInstance().getFriendManager().applyProtection(string);
        } catch (Exception ignored) {}
        return fontRenderer.getStringWidth(string);
    }
}
