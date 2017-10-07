package host.serenity.serenity.api.gui.component;

import host.serenity.serenity.util.RenderUtilities;

public class Checkbox extends BaseComponent {
    private final String label;
    private boolean checked;

    public Checkbox(String label) {
        super(22 + (int) ttfRenderer.getStringWidth(label), 15);

        this.label = label;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        ttfRenderer.drawString(label, getX() + 2, getY() - 1);
        RenderUtilities.drawRect(getX() + getWidth() - getHeight() - 1, getY() + 1,
                getX() + getWidth() - 2, getY() + getHeight() - 1,
                0xFF404040);

        if (checked) {
            RenderUtilities.drawBorderedRect(getX() + getWidth() - getHeight() - 1, getY() + 1,
                    getX() + getWidth() - 2, getY() + getHeight() - 1,
                    0.5F, 0x5F000000, 0xFF18adf3);
        }
    }

    protected void onChecked(boolean checked) {}

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovering(mouseX, mouseY)) {
            this.checked = !this.checked;
            onChecked(checked);
        }
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
