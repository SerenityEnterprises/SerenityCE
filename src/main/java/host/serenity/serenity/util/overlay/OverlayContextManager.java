package host.serenity.serenity.util.overlay;

import host.serenity.serenity.event.internal.GameShutdown;
import host.serenity.serenity.event.render.RenderOverlay;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class OverlayContextManager {
    private Map<OverlayArea, List<RegisteredOverlayRenderer>> overlayRenderers = new HashMap<>();
    private Map<OverlayArea, Boolean> areaIsDirty = new HashMap<>();

    public static OverlayContextManager INSTANCE = new OverlayContextManager();

    public void register(OverlayArea area, double priority, Consumer<OverlayDrawingContext> contextConsumer, BooleanSupplier... conditions) {
        overlayRenderers.putIfAbsent(area, new LinkedList<>());
        overlayRenderers.get(area).add(new RegisteredOverlayRenderer(priority, contextConsumer, conditions));

        areaIsDirty.put(area, true);
    }

    private void render() {
        int padding = 2;

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft(),
                Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        for (OverlayArea area : OverlayArea.values()) {
            int initialX, initialY, deltaY;

            if (area.isTop()) {
                initialY = padding;
                deltaY = 10;
            } else {
                initialY = scaledResolution.getScaledHeight() - padding;
                deltaY = -10;
            }

            if (area.isLeft()) {
                initialX = padding;
            } else {
                initialX = scaledResolution.getScaledWidth() - padding;
            }

            overlayRenderers.putIfAbsent(area, new LinkedList<>());

            List<RegisteredOverlayRenderer> renderers = overlayRenderers.get(area);

            if (areaIsDirty.getOrDefault(area, false)) {
                renderers.sort((r1, r2) -> {
                    if (r1.getPriority() == r2.getPriority()) {
                        System.err.println(String.format("Warning: Priorities are the same (%s)", r1.getPriority()));
                    }

                    // Sort in descending order.
                    return Double.compare(r2.getPriority(), r1.getPriority());
                });
            }

            int x = initialX, y = initialY;

            for (RegisteredOverlayRenderer renderer : renderers) {
                boolean shouldRender = true;

                for (BooleanSupplier condition : renderer.getConditions()) {
                    if (!condition.getAsBoolean()) {
                        shouldRender = false;
                        break;
                    }
                }

                OverlayDrawingContext drawingContext = new OverlayDrawingContext(area, x, y, shouldRender);
                renderer.getContextConsumer().accept(drawingContext);

                x = drawingContext.x;
                y = drawingContext.y;
            }
        }
    }

    public void _registerEvents() {
        EventManager.register(new Listener<RenderOverlay>() {
            @Override
            public void call(RenderOverlay event) {
                render();
            }
        });

        EventManager.register(new Listener<GameShutdown>() {
            @Override
            public void call(GameShutdown event) {
                INSTANCE = new OverlayContextManager();
            }
        });
    }
}

