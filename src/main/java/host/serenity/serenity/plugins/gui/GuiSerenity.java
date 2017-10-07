package host.serenity.serenity.plugins.gui;

import host.serenity.serenity.api.gui.GuiManager;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiSerenity extends GuiScreen {
    private final GuiManager guiManager;

    public GuiSerenity(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);
        guiManager.drawScreen(mouseX, mouseY, partialTicks);

        if (blend) {
            GL11.glEnable(GL11.GL_BLEND);
        } else {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        guiManager.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        guiManager.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE)
            super.keyTyped(typedChar, keyCode);

        guiManager.keyTyped(typedChar, keyCode);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
