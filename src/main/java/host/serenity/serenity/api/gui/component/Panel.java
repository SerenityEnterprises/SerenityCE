package host.serenity.serenity.api.gui.component;

import host.serenity.serenity.util.RenderUtilities;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Panel extends Element {
    protected static final int TITLE_HEIGHT = 15;

    private final String label;
    private List<BaseComponent> components = new CopyOnWriteArrayList<>();

    private int dragX, dragY;
    private boolean dragging;

    public boolean deleting;

    public Panel(String label, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.label = label;
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            setX(dragX + mouseX);
            setY(dragY + mouseY);
        }

        setWidth(25);
        resizePanelToChildren();

        RenderUtilities.drawRect(getX(), getY(), getX() + getWidth(), getY() + TITLE_HEIGHT, 0xFF232323);
        ttfRenderer.drawString(label, getX() + 2, getY() - 1);
        RenderUtilities.drawRect(getX(), getY() + TITLE_HEIGHT, getX() + getWidth(), getY() + getHeight(), 0xFF121212);

        components.forEach(component -> component.setWidth(getWidth()));

        int x = getX();
        int y = getY() + TITLE_HEIGHT;
        for (BaseComponent component : components) {
            component.setX(x);
            component.setY(y);

            y += component.getHeight();
        }

        components.forEach(component -> component.draw(mouseX, mouseY, partialTicks));
    }

    public void resizePanelToChildren() {
        int maxWidth = 25;
        int totalHeight = 0;
        for (BaseComponent component : components) {
            maxWidth = Math.max(maxWidth, component.getWidth());
            totalHeight += component.getHeight();
        }

        setWidth(maxWidth);
        setHeight(totalHeight + TITLE_HEIGHT);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && mouseY <= getY() + TITLE_HEIGHT) {
            dragX = getX() - mouseX;
            dragY = getY() - mouseY;
            dragging = true;
        }

        components.forEach(component -> component.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            dragging = false;
        }

        components.forEach(component -> component.mouseReleased(mouseX, mouseY, mouseButton));
    }

    public void keyTyped(char typedChar, int keyCode) {
        components.forEach(component -> component.keyTyped(typedChar, keyCode));
    }


    protected void delete() {
        this.deleting = true;
    }

    public boolean isDragging() {
        return dragging;
    }

    public List<BaseComponent> getComponents() {
        return components;
    }
}
