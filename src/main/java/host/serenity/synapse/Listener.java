package host.serenity.synapse;

import net.minecraft.client.Minecraft;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BooleanSupplier;

public abstract class Listener<T> {
    private BooleanSupplier conditionalCheck = null;
    protected final Minecraft mc = Minecraft.getMinecraft();
    private Class<T> targetClass;

    public Listener() {
        Type generic = getClass().getGenericSuperclass();
        if (generic instanceof ParameterizedType) {
            for (Type type : ((ParameterizedType) generic).getActualTypeArguments()) {
                if (type instanceof Class) {
                    targetClass = (Class<T>) type;
                    return;
                }
            }
        }
    }

    public Listener(BooleanSupplier conditional) {
        this();

        this.conditionalCheck = conditional;
    }

    public boolean shouldListen() {
        return conditionalCheck == null || conditionalCheck.getAsBoolean();

    }

    public abstract void call(T event);

    public Class<T> getTargetClass() {
        return targetClass;
    }
}
