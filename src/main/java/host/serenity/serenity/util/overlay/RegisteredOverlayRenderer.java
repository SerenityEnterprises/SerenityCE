package host.serenity.serenity.util.overlay;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class RegisteredOverlayRenderer {
    private final double priority;
    private final Consumer<OverlayDrawingContext> contextConsumer;
    private final BooleanSupplier[] conditions;

    public RegisteredOverlayRenderer(double priority, Consumer<OverlayDrawingContext> contextConsumer, BooleanSupplier... conditions) {
        this.priority = priority;
        this.contextConsumer = contextConsumer;
        this.conditions = conditions;
    }

    public double getPriority() {
        return priority;
    }

    public Consumer<OverlayDrawingContext> getContextConsumer() {
        return contextConsumer;
    }

    public BooleanSupplier[] getConditions() {
        return conditions;
    }
}
