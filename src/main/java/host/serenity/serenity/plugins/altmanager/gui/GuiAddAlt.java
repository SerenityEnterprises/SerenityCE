package host.serenity.serenity.plugins.altmanager.gui;

import host.serenity.serenity.plugins.altmanager.SerenityPluginAltManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAddAlt extends GuiScreen {
    private GuiTextField username;
    private GuiMaskedTextField password;

    private GuiScreen parent;

    public GuiAddAlt(GuiScreen parent) {
        this.parent = parent;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, width / 2 - 100,
                height / 4 + 92 + 12, "Add"));
        this.buttonList.add(new GuiButton(1, width / 2 - 100,
                height / 4 + 116 + 12, "Back"));
        this.username = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.username.setMaxStringLength(Integer.MAX_VALUE);
        this.username.setFocused(true);
        this.password = new GuiMaskedTextField(1, this.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
        this.password.setMaxStringLength(Integer.MAX_VALUE);
    }

    public void keyTyped(char character, int keyCode) throws IOException {
        this.username.textboxKeyTyped(character, keyCode);
        this.password.textboxKeyTyped(character, keyCode);
        if (keyCode == Keyboard.KEY_TAB) {
            this.username.setFocused(!this.username.isFocused());
            this.password.setFocused(!this.password.isFocused());
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            actionPerformed((GuiButton) this.buttonList.get(0));
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        username.mouseClicked(mouseX, mouseY, mouseButton);
        password.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(mc.fontRendererObj, "Add Alt", width / 2, 20,
                0xFFFFFFFF);
        if (username.getText().isEmpty()) {
            drawString(mc.fontRendererObj, "Username / Email", width / 2 - 96,
                    66, 0xFF888888);
        }
        if (password.getText().isEmpty()) {
            drawString(mc.fontRendererObj, "Password", width / 2 - 96, 106,
                    0xFF888888);
        }
        username.drawTextBox();
        password.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                if (!username.getText().isEmpty()) {
                    SerenityPluginAltManager.getAccountManager().getAccounts().add(SerenityPluginAltManager.getAccountManager().createAccount(username.getText(), password.getText()));
                }
                mc.displayGuiScreen(parent);
                break;
            case 1:
                mc.displayGuiScreen(parent);
                break;
        }
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void updateScreen() {
        username.updateCursorCounter();
        password.updateCursorCounter();
    }
}
