package host.serenity.serenity.api.gui;

import host.serenity.serenity.api.gui.component.Panel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiManager {
    private List<Panel> panels = new CopyOnWriteArrayList<>();

    public void keyTyped(char typedChar, int keyCode) {
        for (Panel panel : panels) {
            panel.keyTyped(typedChar, keyCode);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Panel panel : panels) {
            boolean active = panel.isHovering(mouseX, mouseY);
            // TODO: only activate it with mouseButton == 0?
            // This mess is to make the ModuleConfigurationPanel on top on creation
            // (which looks like the desired behaviour)
            if (active) {
                panels.remove(panel);
                panels.add(0, panel);
            }
            panel.mouseClicked(mouseX, mouseY, mouseButton);
            if (active) {
                break;
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        panels.removeIf(panel -> panel.deleting);

        for (int i = panels.size() - 1; i >= 0; i--) {
            final Panel panel = panels.get(i);
            panel.draw(mouseX, mouseY, partialTicks);
        }
    }

    public List<Panel> getPanels() {
        return panels;
    }
}
