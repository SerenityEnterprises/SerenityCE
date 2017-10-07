package host.serenity.serenity.api.module;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.binding.impl.ModuleKeybinding;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.module.command.ModuleCommand;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.modules.overlay.ModuleList;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private int colour;
    private final ModuleCategory category;

    private boolean enabled;
    private boolean hidden = false;

    private String display;

    private List<Value<?>> values = new ArrayList<>();
    protected List<Listener<?>> listeners = new ArrayList<>();

    private final List<ModuleMode> moduleModes = new ArrayList<>();
    private int activeModeIndex = 0;

    private ModuleCommand command;

    public Module(String name, int colour, ModuleCategory category) {
        this.display = this.name = name;
        this.colour = colour;
        this.category = category;
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public String getName() {
        return name;
    }

    public int getColour() {
        return colour;
    }

    public List<Value<?>> getValues() {
        return values;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setState(boolean state) {
        if (state != enabled) {
            enabled = state;
            if (enabled) {
                listeners.forEach(EventManager::register);
                if (getActiveMode() != null) {
                    getActiveMode().getListeners().forEach(EventManager::register);
                    try {
                        getActiveMode().onEnable();
                    } catch (Exception ignored) {}
                }

                try {
                    onEnable();
                } catch (Exception ignored) {}
            } else {
                listeners.forEach(EventManager::unregister);
                if (getActiveMode() != null) {
                    getActiveMode().getListeners().forEach(EventManager::unregister);
                    try {
                        getActiveMode().onDisable();
                    } catch (Exception ignored) {}
                }

                try {
                    onDisable();
                } catch (Exception ignored) {}
            }
        }
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        if (!this.display.equals(display)) {
            ModuleList.listDirty = true;
        }

        this.display = display;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public void setActiveMode(ModuleMode mode) {
        if (!moduleModes.contains(mode))
            throw new IllegalArgumentException();

        setActiveMode(moduleModes.indexOf(mode));
    }

    public void setActiveMode(int modeIndex) {
        if (moduleModes.size() <= modeIndex)
            throw new IllegalArgumentException();

        int oldActiveModeIndex = activeModeIndex;
        activeModeIndex = modeIndex;
        setDisplay(String.format("%s %s[%s]", this.getName(), EnumChatFormatting.GRAY, getActiveMode().getName()));

        if (this.isEnabled()) {
            getModuleModes().get(oldActiveModeIndex).getListeners().forEach(EventManager::unregister);
            getModuleModes().get(oldActiveModeIndex).onDisable();
            getModuleModes().get(activeModeIndex).getListeners().forEach(EventManager::register);
            getModuleModes().get(activeModeIndex).onEnable();
        }
    }

    public void setActiveMode(String modeName) {
        setActiveMode(getModuleModes().stream().filter(moduleMode -> moduleMode.getName().equals(modeName)).findFirst().orElse(null));
    }

    public ModuleMode getActiveMode() {
        if (activeModeIndex < 0 || moduleModes.isEmpty())
            return null;

        return moduleModes.get(activeModeIndex);
    }

    public void registerMode(ModuleMode mode) {
        moduleModes.add(mode);
    }

    public List<ModuleMode> getModuleModes() {
        return moduleModes;
    }

    protected void registerToggleKeybinding(int key) {
        Serenity.getInstance().getKeybindManager().register(new ModuleKeybinding(this, key, ModuleKeybinding.Type.TOGGLE));
    }

    private List<CommandBranch> queuedCommandBranches = new ArrayList<>();

    protected void addCommandBranch(CommandBranch branch) {
        queuedCommandBranches.add(branch);
    }

    public final void bootstrap() {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                if (Value.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ModuleValue.class)) {
                        values.add((Value<?>) field.get(this));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.command = new ModuleCommand(this);
        this.command.getBranches().addAll(this.queuedCommandBranches);

        Serenity.getInstance().getCommandManager().getCommands().add(this.command);
    }
}
