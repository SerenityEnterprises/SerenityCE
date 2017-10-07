package host.serenity.serenity.plugins.altmanager.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiMaskedTextField extends GuiTextField {
    public GuiMaskedTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void drawTextBox() {
        String oldText = this.getText();

        StringBuilder newText = new StringBuilder();
        for (char c : oldText.toCharArray()) {
            newText.append("*");
        }

        this.setText(newText.toString());
        super.drawTextBox();
        this.setText(oldText);
    }
}
