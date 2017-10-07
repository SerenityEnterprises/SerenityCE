package host.serenity.serenity.api.gui.component;

import host.serenity.serenity.util.NahrFont;

public class Dropdown extends BaseComponent {
    private static final int CLOSED_HEIGHT = 15;
    private boolean open;

    private final String label;
    private final String[] options;

    private int selectedIndex;

    public Dropdown(String label, String... options) {
        super(75, CLOSED_HEIGHT);
        open = false;

        this.label = label;
        this.options = options;
        this.selectedIndex = 0;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (open) {
            ttfRenderer.drawString(label, getX() + 2, getY() - 1);

            int y = CLOSED_HEIGHT / 2 + (int) ttfRenderer.getStringHeight(label) / 2;
            for (int i = 0; i < options.length; i++) {
                final String option = options[i];

                ttfRenderer.drawString(option, getX() + 6, getY() + y - 1, NahrFont.FontType.NORMAL, i == selectedIndex ? 0xFF18ADF3 : 0xFFFFFFFF, 0x00000000);
                y += ttfRenderer.getStringHeight(option) + 2;
            }
        } else {
            ttfRenderer.drawString(label, getX() + 2, getY() - 1);
        }
        float x = getX() + getWidth() - 1.5F - ttfRenderer.getStringWidth("=");
        ttfRenderer.drawString("=", x, getY() - 2);
        ttfRenderer.drawString("=", x, getY());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if ((mouseButton == 0 || mouseButton == 1) && isHovering(mouseX, mouseY) && (mouseY <= getY() + CLOSED_HEIGHT || mouseButton == 1)) {
            open = !open;
            int height = CLOSED_HEIGHT;
            if (open) {
                height = CLOSED_HEIGHT / 2 + (int) ttfRenderer.getStringHeight(label) / 2;
                for (String string : options) {
                    height += ttfRenderer.getStringHeight(string) + 2;
                }

                height += 2;
            }

            setHeight(height);
        }

        if (mouseButton == 0 && open && isHovering(mouseX, mouseY)) {
            int height = CLOSED_HEIGHT / 2 + (int) ttfRenderer.getStringHeight(label) / 2;
            for (int i = 0; i < options.length; i++) {
                final String option = options[i];

                int topHeight = height;
                height += ttfRenderer.getStringHeight(option) + 2;
                int bottomHeight = height;

                if (mouseY < getY() + bottomHeight && mouseY > getY() + topHeight) {
                    setSelectedIndex(i);
                    onSelectionChanged(getSelected());
                }
            }
        }
    }

    protected void onSelectionChanged(String selection) {}

    public String[] getOptions() {
        return options;
    }

    public String getSelected() {
        if (options.length == 0)
            return null;

        return options[selectedIndex];
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
}
