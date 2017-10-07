package host.serenity.serenity.plugins.gui.component.moduleconfig;

import host.serenity.serenity.api.gui.component.BaseComponent;
import host.serenity.serenity.api.gui.component.Dropdown;
import host.serenity.serenity.api.gui.component.Panel;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.EnumValue;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.api.value.minecraft.BlockListValue;
import host.serenity.serenity.plugins.gui.component.moduleconfig.value.BlockListSelector;
import host.serenity.serenity.plugins.gui.component.moduleconfig.value.BooleanValueCheckbox;
import host.serenity.serenity.plugins.gui.component.moduleconfig.value.ValueTextbox;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleConfigurationPanel extends Panel {
    private final Module module;

    private List<BaseComponent> moduleValueComponents = new ArrayList<>();

    public ModuleConfigurationPanel(Module module, int x, int y) {
        super(module.getName(), x, y, 75, 150);

        this.module = module;

        if (!module.getValues().isEmpty()) {
            addValues(module.getValues().toArray(new Value<?>[module.getValues().size()]), getComponents());
        }

        if (!module.getModuleModes().isEmpty()) {
            // Add a dropdown for the mode.
            List<String> modeNames = new ArrayList<>();
            for (ModuleMode mode : module.getModuleModes()) {
                modeNames.add(mode.getName());
            }

            Dropdown dropdown = new Dropdown("Mode", modeNames.toArray(new String[modeNames.size()])) {
                @Override
                protected void onSelectionChanged(String selection) {
                    for (ModuleMode mode : module.getModuleModes()) {
                        if (mode.getName().equals(selection)) {
                            module.setActiveMode(mode);
                            refreshModeValues();
                        }
                    }
                }
            };

            dropdown.setSelectedIndex(module.getModuleModes().indexOf(module.getActiveMode()));
            getComponents().add(dropdown);
            if (module.getActiveMode().getValues().length > 0) {
                refreshModeValues();
            }
        }

        if (getComponents().size() == 0)
            delete();

        resizePanelToChildren();
        setX(getX() - getWidth() / 2);
        setY(getY() - TITLE_HEIGHT / 2);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 1 && this.isHovering(mouseX, mouseY) && mouseY <= getY() + TITLE_HEIGHT) {
            delete();
        }
    }

    private void refreshModeValues() {
        getComponents().removeAll(moduleValueComponents);
        moduleValueComponents.clear();
        addValues(module.getActiveMode().getValues(), moduleValueComponents);
        getComponents().addAll(moduleValueComponents);
    }

    private void addValues(Value<?>[] values, List<BaseComponent> components) {
        for (Value<?> value : values) {
            if (value instanceof BooleanValue) {
                components.add(new BooleanValueCheckbox((BooleanValue) value));
            } else if (value instanceof EnumValue) {
                EnumValue enumValue = (EnumValue) value;

                Object[] objects = Arrays.stream(enumValue.getConstants()).map(Enum::toString).toArray();
                String[] strings = new String[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    strings[i] = (String) objects[i];
                }

                Dropdown dropdown = new Dropdown(enumValue.getName(), strings) {
                    @Override
                    protected void onSelectionChanged(String selection) {
                        enumValue.setValueFromString(selection);
                    }
                };

                dropdown.setSelectedIndex(ArrayUtils.indexOf(enumValue.getConstants(), enumValue.getValue()));

                components.add(dropdown);
            } else if (value instanceof BlockListValue) {
                components.add(new BlockListSelector(value.getName(), (BlockListValue) value));
            } else {
                components.add(new ValueTextbox(value));
            }
        }
    }

    public Module getModule() {
        return module;
    }
}
