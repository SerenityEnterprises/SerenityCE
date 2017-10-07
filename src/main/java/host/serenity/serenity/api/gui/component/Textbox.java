package host.serenity.serenity.api.gui.component;

import host.serenity.serenity.util.TimeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class Textbox extends BaseComponent {
    private final String label;

    private GuiTextField underlyingTextField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 0, 0);

    {
        underlyingTextField.setMaxStringLength(Integer.MAX_VALUE);
    }

    private TimeHelper blinkTime = new TimeHelper();
    private boolean focused;


    public Textbox(String label) {
        super(Math.max(70, 20 + (int) ttfRenderer.getStringWidth(label)), 15);

        this.label = label;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        String text = label + ": " + underlyingTextField.getText();

        ttfRenderer.drawString(text, getX() + 2, getY());
        if (focused) {
            if (!blinkTime.hasReached(500L)) {
                ttfRenderer.drawString("|", getX() + 1.5F + ttfRenderer.getStringWidth(text.substring(text.length() - underlyingTextField.getCursorPosition())) + ttfRenderer.getStringWidth(label + ": "), getY() - 0.5F);
            }
            if (blinkTime.hasReached(1000L)) {
                blinkTime.reset();
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            focused = isHovering(mouseX, mouseY);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (focused) {
            underlyingTextField.setFocused(true);
            underlyingTextField.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_RETURN) {
                this.focused = false;
                underlyingTextField.setFocused(false);
            }
        }
    }

    public String getText() {
        return underlyingTextField.getText();
    }

    public void setText(String text) {
        underlyingTextField.setText(text);
    }
}
