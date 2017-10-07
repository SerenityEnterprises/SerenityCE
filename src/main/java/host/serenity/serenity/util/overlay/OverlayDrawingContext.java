package host.serenity.serenity.util.overlay;

import host.serenity.serenity.util.NahrFont;
import net.minecraft.client.Minecraft;

public class OverlayDrawingContext {
    private static NahrFont font = new NahrFont("Roboto", 18);

    private Minecraft mc = Minecraft.getMinecraft();

    private final OverlayArea area;
    public int x, y;

    private final boolean shouldRender;

    OverlayDrawingContext(OverlayArea area, int x, int y, boolean shouldRender) {
        this.area = area;
        this.x = x;
        this.y = y;

        this.shouldRender = shouldRender;
    }

    public void addHeight(int height) {
        y += area.isTop() ? height : -height;
    }

    public void drawTTFString(String text, int colour) {
        float height = font.getStringHeight(text);
        float width = font.getStringWidth(text);

        int xPos = (area.isLeft() ? x : x - ((int) Math.ceil(width)));
        int yPos = y;

        if (shouldRender)
            font.drawString(text, xPos, yPos - (height / 2) + 2, NahrFont.FontType.NORMAL, 0xFF000000 | colour, 0);

        addHeight((int) Math.ceil(height));
    }

    public void drawString(String text, int colour, boolean shadow) {
        if (shouldRender)
            mc.fontRendererObj.drawString(text, (area.isLeft() ? x : x - mc.fontRendererObj.getStringWidth(text)), (area.isTop() ? y : y - 10), colour, shadow);

        addHeight(10);
    }
}
