package host.serenity.serenity.api.gui.component;

public class Label extends BaseComponent {
    private final String text;

    public Label(String text) {
        super(2 + (int) ttfRenderer.getStringWidth(text), 2 + (int) ttfRenderer.getStringHeight(text));
        this.text = text;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        ttfRenderer.drawString(text, getX() + 2, getY() - 2);
    }
}
