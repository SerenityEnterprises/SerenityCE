package host.serenity.serenity.plugins.gui.component;

import com.google.common.collect.ImmutableList;
import host.serenity.serenity.api.gui.component.BaseComponent;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.plugins.gui.SerenityPluginGui;
import host.serenity.serenity.plugins.gui.component.moduleconfig.ModuleConfigurationPanel;
import host.serenity.serenity.util.NahrFont;
import host.serenity.serenity.util.RenderUtilities;

public class ModuleButton extends BaseComponent {
    private final Module module;

    public ModuleButton(Module module) {
        super(75, 15);

        this.module = module;

        setWidth((int) Math.max(this.getWidth(), ttfRenderer.getStringWidth(module.getName()) + 8 + ttfRenderer.getStringWidth("...")));
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        RenderUtilities.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF121212);
        ttfRenderer.drawString(module.getName(), getX() + 3, getY() + getHeight() / 2 - ttfRenderer.getStringHeight(module.getName()) + 3, NahrFont.FontType.NORMAL, module.isEnabled() ? 0xFF18ADF3 : 0xFFEFEFEF, 0x00000000);
        if (!module.getValues().isEmpty() || (module.getActiveMode() != null && module.getActiveMode().getValues().length > 0) || !module.getModuleModes().isEmpty()) {
            ttfRenderer.drawString("...", getX() + getWidth() - ttfRenderer.getStringWidth("...") - 3, getY() - 2F, NahrFont.FontType.NORMAL, 0xFFEEEEEE, 0x00000000);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovering(mouseX, mouseY)) {
            if (mouseButton == 0) {
                module.setState(!module.isEnabled());
            }

            if (mouseButton == 1) {
                ImmutableList.copyOf(SerenityPluginGui.getGuiManager().getPanels()).stream()
                        .filter(panel -> panel instanceof ModuleConfigurationPanel && ((ModuleConfigurationPanel) panel).getModule() == module)
                        .forEach(SerenityPluginGui.getGuiManager().getPanels()::remove);

                SerenityPluginGui.getGuiManager().getPanels().add(0, new ModuleConfigurationPanel(module, mouseX, mouseY));
            }
        }
    }
}
