package host.serenity.serenity.api.gui.component;

public abstract class BaseComponent extends Element {
    public BaseComponent(int width, int height) {
        super(0, 0, width, height);
    }

    public abstract void draw(int mouseX, int mouseY, float partialTicks);

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}
    public void keyTyped(char typedChar, int keyCode) {}
}
