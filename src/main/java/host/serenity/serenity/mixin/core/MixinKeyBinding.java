package host.serenity.serenity.mixin.core;

import host.serenity.serenity.util.iface.KeyBindingExtension;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding implements KeyBindingExtension {
    @Shadow
    private boolean pressed;

    @Override
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
}
