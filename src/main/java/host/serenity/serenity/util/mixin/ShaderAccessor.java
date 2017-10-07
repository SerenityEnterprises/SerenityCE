package host.serenity.serenity.util.mixin;

import java.lang.reflect.Field;

public class ShaderAccessor {
    public static Field getShadowPassField() {
        try {
            Class shadersClass = Class.forName("shadersmod.client.Shaders");
            return shadersClass.getDeclaredField("isShadowPass");
        } catch (Exception ignored) {
        }

        return null;
    }
}
