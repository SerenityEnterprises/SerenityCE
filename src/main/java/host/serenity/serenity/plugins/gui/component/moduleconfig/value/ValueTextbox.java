package host.serenity.serenity.plugins.gui.component.moduleconfig.value;

import host.serenity.serenity.api.gui.component.Textbox;
import host.serenity.serenity.api.value.Value;
import org.lwjgl.input.Keyboard;

public class ValueTextbox extends Textbox {
    private final Value<?> value;

    public ValueTextbox(Value<?> value) {
        super(value.getName());
        this.value = value;

        setText(value.getValue().toString());

        value.getChangeListeners().add(oldValue -> setText(value.getValue().toString()));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!isHovering(mouseX, mouseY))
            value.setValueFromString(this.getText());

        setText(value.getValue().toString());
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_RETURN) {
            value.setValueFromString(this.getText());
            setText(value.getValue().toString());
        }
    }
}
